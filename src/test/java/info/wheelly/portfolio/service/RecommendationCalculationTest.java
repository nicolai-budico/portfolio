package info.wheelly.portfolio.service;

import info.wheelly.portfolio.classes.Task;
import info.wheelly.portfolio.dto.AllocationRequest;
import info.wheelly.portfolio.dto.AllocationResult;
import info.wheelly.portfolio.dto.Transfer;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendationCalculationTest {

    @Test
    public void calculateTest() {
        Task task = new Task()
                .setRequest(new AllocationRequest().setRequest(generateRecommendationsRequest()));
        RecommendationServiceImpl.internalPerformCalculation(task);

        Assert.assertNotNull(task.getResult());
        Assert.assertNotNull(task.getResult().getResult());
        Assert.assertEquals(
                task.getRequest().getRequest().size(),
                task.getResult().getResult().size()
        );

        Double oldTotalAmount =  task.getResult().getResult().stream()
                .map(AllocationResult.Item::getOldAmount)
                .reduce((a, b) -> a + b)
                .orElse(1.0);

        Double newTotalAmount =  task.getResult().getResult().stream()
                .map(AllocationResult.Item::getOldAmount)
                .reduce((a, b) -> a + b)
                .orElse(1.0);

        Assert.assertEquals(oldTotalAmount, newTotalAmount);

        task.getResult().getResult().forEach(item -> {
            // Assert that new amount matches desired percentage
            Assert.assertEquals((oldTotalAmount * item.getToleranceScore()/100), item.getNewAmount(), 0.000001);
        });

        // Test that transfers make correct amounts
        Map<String, Double> categoriesAmount = task.getResult().getResult().stream().collect(Collectors.toMap(
                AllocationResult.Item::getCategoryId,
                AllocationResult.Item::getOldAmount
        ));

        for (Transfer transfer: task.getResult().getTransfers()) {
            Double amount;
            amount = categoriesAmount.get(transfer.getSource());
            categoriesAmount.put(transfer.getSource(), amount - transfer.getAmount());

            amount = categoriesAmount.get(transfer.getDestination());
            categoriesAmount.put(transfer.getDestination(), amount + transfer.getAmount());
        }

        task.getResult().getResult().forEach(item -> {
            Double amount = categoriesAmount.get(item.getCategoryId());
            // Assert that new amount matches desired percentage
            Assert.assertEquals(item.getNewAmount(), amount, 0.000001);
        });

    }

    private List<AllocationRequest.Item> generateRecommendationsRequest() {
        Iterator<Double> amounts = Arrays.asList(10.0, 20.0, 30.0, 40.0, 0.0).iterator();
        Iterator<Double> scores  = Arrays.asList(20.0, 25.0, 25.0, 25.0, 5.0).iterator();

        int categoriesCount = 5;
        List<AllocationRequest.Item> result = new ArrayList<>();

        for (int k = 1 ; k <= categoriesCount ; k++) {
            result.add(new AllocationRequest.Item()
                    .setAmount(amounts.next())
                    .setToleranceScore(scores.next())
                    .setCategoryId("C" + k)
            );
        }

        return result;
    }
}
