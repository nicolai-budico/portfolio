package info.wheelly.portfolio.dto;

import info.wheelly.portfolio.validation.ToleranceScoreSum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(value = "AllocationRequest", description = "Represents recommendation request")
@Getter
@Setter
@Accessors(chain = true)
public class AllocationRequest {

    @ApiModel(value = "AllocationRequest.Item", description = "Describes each investment category amount and tolerance score")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class Item {
        @ApiModelProperty("Category ID")
        @NotEmpty(message = "Category ID can't be empty")
        private String categoryId;

        @ApiModelProperty(value = "Current amount", notes = "Not negative value")
        @NotNull(message = "Amount should be specified")
        private Double amount;

        @ApiModelProperty(value = "Tolerance score", notes = "Tolerance score in percent, e.g. 20, 50 or 100")
        @NotNull(message = "Tolerance Score should be between 0 and 100 inclusive")
        @Min(value = 0, message = "Tolerance Score should be between 0 and 100 inclusive")
        @Max(value = 100, message = "Tolerance Score should be between 0 and 100 inclusive")
        private Double toleranceScore;
    }

    @ApiModelProperty(value = "List of categories with their amounts and tolerance score", notes = "tolerance scores across all items should be 100% in sum")
    @NotNull(message = "Request parameter should be specified")
    @ToleranceScoreSum()
    @Valid
    private List<Item> request;
}
