package info.wheelly.portfolio.repository;

import info.wheelly.portfolio.dto.TaskStatus;
import info.wheelly.portfolio.classes.Task;

import java.util.List;
import java.util.Map;

public interface TaskRepository {
    /**
     * Stores tasks into database or other underline storage
     *
     * @param task task to be stored
     * @return new version of task
     */
    Task storeTask(Task task);

    /**
     * Receives task information from underline storage and returns to the caller
     *
     * @param id task ID
     * @return task object
     */
    Task getTask(String id);

    /**
     * Receives tasks that are in status {@link TaskStatus#QUEUED}, mark them as {@link TaskStatus#RUNNING} and returns
     * to the caller.
     * Selection and update are atomically, so another threads wouldn't take the same tasks even called this method in
     * the same time
     *
     * @param maxCount the maximum number of tasks to be returned
     * @return List of tasks
     */
    List<Task> takeTasks(int maxCount);

    /**
     * Returns tasks count summary grouped by task status
     *
     * @return Global status information
     */
    Map<TaskStatus, Long> getStats();
}
