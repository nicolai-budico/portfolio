package info.wheelly.portfolio.classes;

import info.wheelly.portfolio.dto.TaskStatus;
import info.wheelly.portfolio.dto.AllocationRequest;
import info.wheelly.portfolio.dto.AllocationResult;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class Task {
    private String taskId;
    private TaskStatus status;
    private AllocationRequest request;
    private AllocationResult result;
}
