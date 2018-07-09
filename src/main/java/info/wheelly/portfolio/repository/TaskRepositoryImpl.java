package info.wheelly.portfolio.repository;

import info.wheelly.portfolio.dto.TaskStatus;
import info.wheelly.portfolio.utils.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import info.wheelly.portfolio.classes.Task;
import info.wheelly.portfolio.dto.AllocationRequest;
import info.wheelly.portfolio.dto.AllocationResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TaskRepositoryImpl implements TaskRepository {
    private static final Logger LOG = LoggerFactory.getLogger(TaskRepositoryImpl.class);

    private static class TaskRecord {
        private final String taskId;
        private final TaskStatus status;
        private final String request;
        private final String result;

        private TaskRecord(String taskId, TaskStatus status, String request, String result) {
            this.taskId = taskId;
            this.status = status;
            this.request = request;
            this.result = result;
        }

        @Override
        public int hashCode() {
            return taskId.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof TaskRecord) {
                return StringUtils.equals(this.taskId, ((TaskRecord) other).taskId);
            }
            else {
                return false;
            }
        }
    }

    private final Map<String, TaskRecord> data = new ConcurrentHashMap<>();

    @Override
    public Task storeTask(Task task) {
        LOG.debug("Store task '{}' in database", task.getTaskId());
        TaskRecord record = data.compute(task.getTaskId(), (key, old) -> new TaskRecord(
                task.getTaskId(),
                task.getStatus(),
                JSON.toJson(task.getRequest()),
                JSON.toJson(task.getResult())
        ));

        return recordsToTask(record);
    }

    @Override
    public Task getTask(String id) {
        LOG.debug("Requested task '{}' from database", id);
        TaskRecord record = data.get(id);
        if (record == null) {
            return null;
        }

        return recordsToTask(record);
    }

    @Override
    public List<Task> takeTasks(int maxCount) {
        LOG.trace("Take queued tasks from database");
        List<TaskRecord> result = new ArrayList<>();
        data.replaceAll((taskId, taskRecord) -> {
            if (TaskStatus.QUEUED.equals(taskRecord.status) && result.size() < maxCount) {
                result.add(taskRecord);
                return new TaskRecord(
                        taskRecord.taskId,
                        TaskStatus.RUNNING,
                        taskRecord.request,
                        taskRecord.result
                );
            }
            else {
                return taskRecord;
            }
        });

        LOG.trace("Found {} queued tasks", result.size());
        return result.stream()
                .map(this::recordsToTask)
                .collect(Collectors.toList());
    }

    @Override
    public Map<TaskStatus, Long> getStats() {
        LOG.debug("Requested task stats from database");
        Map<TaskStatus, Long> result = data.values().stream()
                .collect(Collectors.groupingBy(r -> r.status, Collectors.counting()));

        for(TaskStatus status: TaskStatus.values()) {
            if (!result.containsKey(status)) {
                result.put(status, 0L);
            }
        }

        LOG.debug("Current stats: {}", String.join(",", result.entrySet().stream()
                .map(kv -> String.format("%s: %d", kv.getKey(), kv.getValue()))
                .collect(Collectors.toList())
        ));
        return result;
    }

    private Task recordsToTask(TaskRecord record) {
        return new Task()
                .setTaskId(record.taskId)
                .setStatus(record.status)
                .setRequest(JSON.fromJson(record.request, AllocationRequest.class))
                .setResult(JSON.fromJson(record.result, AllocationResult.class));
    }
}
