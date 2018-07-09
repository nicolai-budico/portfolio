package info.wheelly.portfolio.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import info.wheelly.portfolio.dto.Category;
import info.wheelly.portfolio.dto.Portfolio;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<Category> listCategories() {
        // Receive categories from database
        List<Category> result = Arrays.asList(
                new Category().setId("IC1").setTitle("Bonds"),
                new Category().setId("IC2").setTitle("Large Cap"),
                new Category().setId("IC3").setTitle("Mid Cap"),
                new Category().setId("IC4").setTitle("Foreign"),
                new Category().setId("IC5").setTitle("Small Cap")
        );

        return result.stream()
                .sorted((a ,b) -> StringUtils.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Portfolio> listPortfolios() {
        return Arrays.asList(
                new Portfolio(1, Arrays.asList(
                        new Portfolio.Item("IC1", 80.0),
                        new Portfolio.Item("IC2", 20.0),
                        new Portfolio.Item("IC3",  0.0),
                        new Portfolio.Item("IC4",  0.0),
                        new Portfolio.Item("IC5",  0.0)
                )),
                new Portfolio(2, Arrays.asList(
                        new Portfolio.Item("IC1", 70.0),
                        new Portfolio.Item("IC2", 15.0),
                        new Portfolio.Item("IC3", 15.0),
                        new Portfolio.Item("IC4",  0.0),
                        new Portfolio.Item("IC5",  0.0)
                )),
                new Portfolio(3, Arrays.asList(
                        new Portfolio.Item("IC1", 60.0),
                        new Portfolio.Item("IC2", 15.0),
                        new Portfolio.Item("IC3", 15.0),
                        new Portfolio.Item("IC4", 10.0),
                        new Portfolio.Item("IC5",  0.0)
                )),
                new Portfolio(4, Arrays.asList(
                        new Portfolio.Item("IC1", 50.0),
                        new Portfolio.Item("IC2", 20.0),
                        new Portfolio.Item("IC3", 20.0),
                        new Portfolio.Item("IC4", 10.0),
                        new Portfolio.Item("IC5",  0.0)
                )),
                new Portfolio(5, Arrays.asList(
                        new Portfolio.Item("IC1", 40.0),
                        new Portfolio.Item("IC2", 20.0),
                        new Portfolio.Item("IC3", 20.0),
                        new Portfolio.Item("IC4", 20.0),
                        new Portfolio.Item("IC5",  0.0)
                )),
                new Portfolio(6, Arrays.asList(
                        new Portfolio.Item("IC1", 35.0),
                        new Portfolio.Item("IC2", 25.0),
                        new Portfolio.Item("IC3",  5.0),
                        new Portfolio.Item("IC4", 30.0),
                        new Portfolio.Item("IC5",  5.0)
                )),
                new Portfolio(7, Arrays.asList(
                        new Portfolio.Item("IC1", 20.0),
                        new Portfolio.Item("IC2", 25.0),
                        new Portfolio.Item("IC3", 25.0),
                        new Portfolio.Item("IC4", 25.0),
                        new Portfolio.Item("IC5",  5.0)
                )),
                new Portfolio(8, Arrays.asList(
                        new Portfolio.Item("IC1", 10.0),
                        new Portfolio.Item("IC2", 20.0),
                        new Portfolio.Item("IC3", 40.0),
                        new Portfolio.Item("IC4", 20.0),
                        new Portfolio.Item("IC5", 10.0)
                )),
                new Portfolio(9, Arrays.asList(
                        new Portfolio.Item("IC1",  5.0),
                        new Portfolio.Item("IC2", 15.0),
                        new Portfolio.Item("IC3", 40.0),
                        new Portfolio.Item("IC4", 25.0),
                        new Portfolio.Item("IC5", 15.0)
                )),
                new Portfolio(10, Arrays.asList(
                        new Portfolio.Item("IC1",  0.0),
                        new Portfolio.Item("IC2",  5.0),
                        new Portfolio.Item("IC3", 25.0),
                        new Portfolio.Item("IC4", 30.0),
                        new Portfolio.Item("IC5", 40.0)
                ))
        );
    }
}
