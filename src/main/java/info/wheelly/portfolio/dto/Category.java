package info.wheelly.portfolio.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel("InvestmentCategory")
@Getter
@Setter
@Accessors(chain = true)
public class Category {
    @ApiModelProperty("Category id")
    private String id;

    @ApiModelProperty("Category title")
    private String title;
}
