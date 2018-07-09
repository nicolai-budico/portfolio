package info.wheelly.portfolio.service;

import info.wheelly.portfolio.dto.Portfolio;
import org.springframework.stereotype.Service;
import info.wheelly.portfolio.dto.Category;

import java.util.List;

@Service
public interface CategoryService {
    /**
     * Return a list of pre-defined investment categories
     *
     * @return list of pre-defined categories
     */
    List<Category> listCategories();

    /**
     * Return a list of pre-defined recommended portfolios
     *
     * @return list of pre-defined portfolios
     */
    List<Portfolio> listPortfolios();
}
