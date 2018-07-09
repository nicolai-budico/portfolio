package info.wheelly.portfolio.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@ApiModel(value = "Portfolio", description = "Provides relative funds allocation between wallets (%)")
@Getter
@Setter
@Accessors(chain = true)
public class Portfolio {

    @ApiModel(value = "RiskLevelItem", description = "Describes tolerance score of particular category")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Item {
        @ApiModelProperty("Category id")
        private String categoryId;

        @ApiModelProperty("Tolerance Score")
        private Double toleranceScore;

        public Item() {
        }

        public Item(String categoryId, Double toleranceScore) {
            this.categoryId = categoryId;
            this.toleranceScore = toleranceScore;
        }
    }

    public Portfolio() {
    }

    public Portfolio(Integer level, List<Item> allocation) {
        this.level = level;
        this.allocation = allocation;
    }

    @ApiModelProperty("Level ID")
    private Integer level;

    @ApiModelProperty("Tolerance score info")
    private List<Item> allocation;
}
