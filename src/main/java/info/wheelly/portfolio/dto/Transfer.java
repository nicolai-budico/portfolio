package info.wheelly.portfolio.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel(value = "Transfer", description = "Represents single transfer between investment categories")
@Getter
@Setter
@Accessors(chain = true)
public class Transfer {
    @ApiModelProperty("Source investment category ID")
    private String source;

    @ApiModelProperty("Destination investment category ID")
    private String destination;

    @ApiModelProperty("Amount to be transfered")
    private Double amount;
}
