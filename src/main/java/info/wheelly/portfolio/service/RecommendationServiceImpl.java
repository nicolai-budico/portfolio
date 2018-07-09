package info.wheelly.portfolio.service;

import info.wheelly.portfolio.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import info.wheelly.portfolio.classes.Task;
import info.wheelly.portfolio.repository.TaskRepository;

import javax.annotation.PreDestroy;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final Semaphore semaphore;
    private final ExecutorService queueListener;
    private final Future<Void> queueListenerWatch;

    private final ExecutorService executor;

    private final TaskRepository repository;

    private final Long throttleMs;

    public RecommendationServiceImpl(TaskRepository repository, Integer maxTasks, Long throttleMs) {
        LOG.debug("Initializing RecommendationService with maxTasks: {} and throttleMs: {}", maxTasks, throttleMs);
        this.throttleMs = throttleMs;
        this.semaphore = new Semaphore(maxTasks);
        this.executor = new ThreadPoolExecutor(maxTasks, 2*maxTasks, 1L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>()
        );

        this.queueListener = Executors.newSingleThreadExecutor();
        this.queueListenerWatch = this.queueListener.submit(this::listenQueue);

        this.repository = repository;
    }

    @Override
    public TaskInfo submitTask(@NotNull @Valid AllocationRequest allocationRequest) {
        LOG.debug("Requested recommendation calculation");

        // Prepare task parameters
        Task task = new Task()
                .setTaskId(UUID.randomUUID().toString())
                .setStatus(TaskStatus.QUEUED)
                .setRequest(allocationRequest)
                .setResult(null);


        // There are no calculations when number of categories equals to 1
        if (allocationRequest.getRequest().size() <= 1) {
            internalPerformCalculation(task);
            task.setStatus(TaskStatus.COMPLETED);

            // Insert task into database
            task = repository.storeTask(task);
        }
        // If we can, we submit calculation immediately
        else if (semaphore.tryAcquire(1)) {
            LOG.debug("Acquired permit from semaphore. Execute task immediate.");
            // Mark as running to prevent conflicts with selection tasks from queue
            task.setStatus(TaskStatus.RUNNING);

            // Insert task into database
            task = repository.storeTask(task);

            // Submit task into execution service to calculate recommendations
            executor.submit(new TaskWrapper(task));
        }
        else {
            // We were unable to submit task immediately, so put it into database
            LOG.debug("There are no free execution slots. Queue task.");
            repository.storeTask(task);
        }

        return getTaskInfo(task.getTaskId());
    }

    @Override
    public TaskInfo getTaskInfo(String taskId) {
        LOG.debug("Requested task status: '{}'.", taskId);
        Task task = repository.getTask(taskId);
        if (task != null) {
            LOG.debug("Task found with status: '{}'.", task.getStatus());
            return new TaskInfo()
                    .setTaskId(task.getTaskId())
                    .setStatus(task.getStatus());
        }
        else {
            LOG.debug("Task wasn't found.");
            return null;
        }
    }

    @Override
    public AllocationResult getTaskResult(String taskId) {
        LOG.debug("Requested task result: '{}'.", taskId);
        Task task = repository.getTask(taskId);
        if (task != null && TaskStatus.COMPLETED.equals(task.getStatus())) {
            LOG.debug("Task '{}' found and is completed.", taskId);
            return task.getResult();
        }
        else {
            // There are no result yet
            LOG.debug("Task '{}' wasn't found or isn't completed yet.", taskId);
            return null;
        }
    }

    @Override
    public Map<TaskStatus, Long> getStats() {
        LOG.debug("Requested stats.");
        return repository.getStats();
    }

    @PreDestroy
    protected void finalize() {
        LOG.debug("Finalize execution daemon.");

        // Stop queue listener
        LOG.debug("Shutdown queue listener.");
        if (!queueListenerWatch.isDone()) {
            queueListenerWatch.cancel(true);
        }
        queueListener.shutdown();
        try {
            queueListener.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException interrupt) {
            // Timeout. Force shutdown
            queueListener.shutdownNow();
        }
        LOG.debug("Queue listener stopped.");

        // Stop executor service and prevent new calculations
        LOG.debug("Shutdown working threads.");
        executor.shutdown();

        try {
            LOG.debug("Wait for working threads are stopped.");
            // Wait all for threads are completed
            executor.awaitTermination(1, TimeUnit.MINUTES);
            LOG.debug("Working threads stopped.");
        }
        catch (InterruptedException interrupt) {
            LOG.debug("Timeout exceeded while waiting for running threads. Force shutdown.");
            // Threads still running but program termination requested
            List<Runnable> cancelledTasks = executor.shutdownNow();
            // Move all cancelled tasks back to the queue
            for(Runnable task: cancelledTasks) {
                LOG.debug("Return cancelled task '{}' back to the queue.", ((TaskWrapper)task).task.getTaskId());
                ((TaskWrapper)task).task.setStatus(TaskStatus.QUEUED);
                repository.storeTask(((TaskWrapper)task).task);
            }
        }

        LOG.debug("Execution daemon stopped.");
    }

    /**
     * Performs allocation calculation based on provided percents weight of each investment category
     *
     * @param task returns parameter <code>task</code>
     */
    static void internalPerformCalculation(Task task) {
        Double totalAmount = task.getRequest().getRequest().stream()
                .map(AllocationRequest.Item::getAmount)
                .reduce((s1, s2) -> s1 + s2)
                .orElse(0.0);

        AllocationResult allocationResult = new AllocationResult()
                .setResult(task.getRequest().getRequest().stream()
                        .map(stat -> new AllocationResult.Item(
                                stat.getCategoryId(),
                                stat.getToleranceScore(),
                                stat.getAmount(),
                                totalAmount * stat.getToleranceScore() / 100.0,
                                totalAmount * stat.getToleranceScore() / 100.0 - stat.getAmount()
                        ))
                        .collect(Collectors.toList())
                );
        task.setResult(allocationResult);

        List<Transfer> transfers = new ArrayList<>();
        allocationResult.setTransfers(transfers);

        if (task.getRequest().getRequest().size() <= 1) {
            // In case of zero or single category, allocation is already optimal
            return;
        }

        Map<String, Double> sources = allocationResult.getResult().stream()
                .filter(i -> i.getDifference() < 0)
                .collect(Collectors.toMap(AllocationResult.Item::getCategoryId, i -> Math.abs(i.getDifference())));

        Map<String, Double> destinations = allocationResult.getResult().stream()
                .filter(i -> i.getDifference() > 0)
                .collect(Collectors.toMap(AllocationResult.Item::getCategoryId, i -> Math.abs(i.getDifference())));

        while (sources.size() > 0 && destinations.size() > 0) {
            Map.Entry<String, Double> betterSource = null;
            Map.Entry<String, Double> betterDestination = null;
            Double betterDiff = Double.MAX_VALUE;

            for (Map.Entry<String, Double> source : sources.entrySet()) {
                for (Map.Entry<String, Double> destination : destinations.entrySet()) {
                    Double currentDiff = Math.abs(source.getValue() - destination.getValue());
                    if (currentDiff < betterDiff) {
                        betterDiff = currentDiff;
                        betterSource = source;
                        betterDestination = destination;
                    }
                }
            }

            // It is impossible that betterSource == null but code analysis tools may raise warnings
            // when accessing betterSource or betterDestination
            assert betterSource != null;

            Double transferAmount = Math.min(betterDestination.getValue(), betterSource.getValue());
            if (transferAmount.equals(betterDestination.getValue())) {
                destinations.remove(betterDestination.getKey());
            }
            else {
                destinations.put(betterDestination.getKey(), betterDestination.getValue() - transferAmount);
            }

            if (transferAmount.equals(betterSource.getValue())) {
                sources.remove(betterSource.getKey());
            }
            else {
                sources.put(betterSource.getKey(), betterSource.getValue() - transferAmount);
            }

            transfers.add(new Transfer()
                    .setSource(betterSource.getKey())
                    .setDestination(betterDestination.getKey())
                    .setAmount(transferAmount)
            );
        }
    }

    /**
     * Listens the queue and submits tasks into ExecutorService
     * @return null
     */
    private Void listenQueue() {
        LOG.debug("Enter queue listener");
        try {
            while (!executor.isShutdown()) {
                int permits;

                // Assume that working with tasks storage is heavy operation like database select
                // So we are determine if there are free execution slots before fetching tasks from
                // database
                if ((permits = semaphore.drainPermits()) == 0) {
                    // There are no free slots to perform calculations
                    // Wait 200 ms and repeat iteration
                    Thread.sleep(200L);
                    continue;
                }

                LOG.trace("Obtained {} permits from semaphore.", permits);

                // Storage level errors may appear but not are covered here
                // Solution: Handle error, sleep for exponential backoff delay, skip current iteration
                //           Count continuous errors count and break the loop after pre-defined limit of
                //           re-tries
                //           Once "repository.takeTasks" returned no errors, reset continuous errors counter
                List<Task> tasks = repository.takeTasks(permits);

                // Return extra permits back to the semaphore
                if (permits > tasks.size()) {
                    LOG.trace("Received {} tasks from queue. Return extra {} permits back to semaphore",
                            tasks.size(),
                            permits - tasks.size()
                    );
                    semaphore.release(permits - tasks.size());
                }

                if (tasks.isEmpty()) {
                    // Wait 200 ms and repeat iteration
                    Thread.sleep(200L);
                    continue;
                }

                int k = 0;
                for ( ; k < tasks.size() ; k++) {
                    Task task = tasks.get(k);
                    LOG.debug("Submit task '{}' to execution.", task.getTaskId());
                    try {
                        executor.submit(new TaskWrapper(task));
                    }
                    catch (RejectedExecutionException reject) {
                        // RejectedExecutionException may be thrown in this place in two cases:
                        // 1. When ExecutorService isShutdown()
                        // 2. There are no left space in the underline blocking queue.
                        // Second case is impossible because ExecutorService was initialized with threads limit grater
                        // then maximum allowable processes
                        LOG.debug("Task execution rejected. Break the listener loop");
                        break;
                    }
                }

                // Return remain tasks back to the queue
                for ( ; k < tasks.size() ; k++) {
                    Task task = tasks.get(k);
                    repository.storeTask(task);
                }

                Thread.sleep(0L);
            }
        }
        catch (InterruptedException interrupt) {
            // Thread interrupted. Exit listener.
            LOG.debug("Queue listener thread interrupted.");
        }
        catch (RuntimeException error) {
            // Something went wrong.
            LOG.debug("Queue listener thread interrupted by error.", error);
        }
        finally {
            LOG.debug("Exit queue listener thread.");
        }

        return null;
    }

    /**
     * Wrapper that provides access to the task in finalization method
     */
    private class TaskWrapper implements Callable<Task> {

        private final Task task;

        private TaskWrapper(Task task) {
            this.task = task;
        }

        @Override
        public Task call() {
            try {
                LOG.debug("Perform calculations for task '{}'.", task.getTaskId());

                try { Thread.sleep(throttleMs); }
                catch (InterruptedException interrupt) {
                    Thread.currentThread().interrupt();
                }

                try {
                    internalPerformCalculation(task);
                }
                catch (Throwable error) {
                    LOG.error("Unhandled error occurred: ", error);
                }

                task.setStatus(TaskStatus.COMPLETED);
                LOG.debug("Calculations for task '{}' completed successfully.", task.getTaskId());
                return repository.storeTask(task);
            }
            finally {
                semaphore.release();
            }
        }
    }
}
