package info.wheelly.portfolio.validation;

import info.wheelly.portfolio.dto.AllocationRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ToleranceScoreSumValidator implements ConstraintValidator<ToleranceScoreSum, List<AllocationRequest.Item>> {
    @Override
    public void initialize(ToleranceScoreSum constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<AllocationRequest.Item> items, ConstraintValidatorContext constraintValidatorContext) {
        return 100 == items.stream()
                .map(AllocationRequest.Item::getToleranceScore)
                .reduce((s1, s2) -> s1 + s2)
                .orElse(0.0);
    }
}
