package info.wheelly.portfolio.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel(value = "TaskInfo")
@Getter
@Setter
@Accessors(chain = true)
public class TaskInfo {
    @ApiModelProperty("Allocation task ID")
    private String taskId;

    @ApiModelProperty("Allocation task status")
    private TaskStatus status;
}
