package info.wheelly.portfolio.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@ApiModel(value = "AllocationResult", description = "Represents allocation result")
@Getter
@Setter
@Accessors(chain = true)
public class AllocationResult {

    @ApiModel(value = "AllocationResult.Item", description = "Describes results of single allocation item")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class Item {
        @ApiModelProperty("Category id")
        private String categoryId;

        @ApiModelProperty("Tolerance Score")
        private Double toleranceScore;

        @ApiModelProperty("Initial amount")
        private Double oldAmount;

        @ApiModelProperty("Final amount")
        private Double newAmount;

        @ApiModelProperty("Difference between final amount and initial amount")
        private Double difference;

        public Item(String categoryId, Double toleranceScore, Double oldAmount, Double newAmount, Double difference) {
            this.categoryId = categoryId;
            this.toleranceScore = toleranceScore;
            this.oldAmount = oldAmount;
            this.newAmount = newAmount;
            this.difference = difference;
        }

        public Item() {
        }
    }

    @ApiModelProperty("List of allocation items")
    List<Item> result;

    @ApiModelProperty("List of transfers that should be performed to bring desired money allocation")
    List<Transfer> transfers;
}
