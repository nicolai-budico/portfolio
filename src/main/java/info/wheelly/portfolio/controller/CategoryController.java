package info.wheelly.portfolio.controller;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import info.wheelly.portfolio.dto.Category;
import info.wheelly.portfolio.dto.Portfolio;
import info.wheelly.portfolio.service.CategoryService;

import java.util.List;

@Api(tags = "Directory Controller")
@RestController
@RequestMapping("/")
public class CategoryController {

    private  final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @RequestMapping(
            value = "/categories",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Category>> listCategories() {
        List<Category> result = categoryService.listCategories();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/portfolios",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Portfolio>> listPortfolios() {
        List<Portfolio> result = categoryService.listPortfolios();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
