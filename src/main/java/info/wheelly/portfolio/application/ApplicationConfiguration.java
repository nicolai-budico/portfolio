package info.wheelly.portfolio.application;


import info.wheelly.portfolio.repository.TaskRepository;
import info.wheelly.portfolio.service.RecommendationService;
import info.wheelly.portfolio.service.RecommendationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Value("${execution.max-tasks:2}")
    private Integer executionMaxTasks;

    @Value("${execution.throttle-ms:0}")
    private Long executionThrottleMs;


    private final TaskRepository taskRepository;

    @Autowired
    public ApplicationConfiguration(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Bean
    public RecommendationService recommendationService() {
        return new RecommendationServiceImpl(
                taskRepository,
                executionMaxTasks,
                executionThrottleMs
        );
    }
}
