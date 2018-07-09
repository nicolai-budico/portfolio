package info.wheelly.portfolio.service;

import info.wheelly.portfolio.dto.AllocationRequest;
import info.wheelly.portfolio.dto.TaskInfo;
import info.wheelly.portfolio.dto.TaskStatus;
import org.springframework.stereotype.Service;
import info.wheelly.portfolio.dto.AllocationResult;

import java.util.Map;


@Service
public interface RecommendationService {

    /**
     * Puts calculation task into the tasks queue. In case of single investment category submitted task is calculated
     * immediately and status {@link TaskStatus#COMPLETED} returned.
     * When <code>allocationRequest</code> contains more than one investment category and there are free execution slots
     * in the queue status {@link TaskStatus#RUNNING} returned
     * Else status {@link TaskStatus#QUEUED} would be returned
     *
     * @param allocationRequest Allocation parameters
     * @return Task information: id and status
     */
    TaskInfo submitTask(AllocationRequest allocationRequest);

    /**
     * Returns task information: id and status
     * If there are no task with such <code>taskId</code> method returns <code>null</code>
     *
     * @param taskId id of the task returned from {@link RecommendationService#submitTask(AllocationRequest)}
     * @return Task information: id and status
     */
    TaskInfo getTaskInfo(String taskId);

    /**
     * Returns calculated allocation recommendation
     * If there are no task with such <code>taskId</code> method returns <code>null</code>
     *
     * @param taskId id of the task returned from {@link RecommendationService#submitTask(AllocationRequest)}
     * @return Task information: id and status
     */
    AllocationResult getTaskResult(String taskId);

    /**
     * Returns tasks count summary grouped by task status
     *
     * @return Global status information
     */
    Map<TaskStatus, Long> getStats();
}
