package info.wheelly.portfolio.controller;

import info.wheelly.portfolio.dto.TaskStatus;
import info.wheelly.portfolio.dto.AllocationRequest;
import info.wheelly.portfolio.dto.AllocationResult;
import info.wheelly.portfolio.dto.TaskInfo;
import info.wheelly.portfolio.exceptions.APIException;
import info.wheelly.portfolio.service.RecommendationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Map;


@Api(tags = "Recommendations Controller")
@RestController
@RequestMapping(value = "/recommendations")
public class RecommendationController {

    private  final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @ApiOperation(
            value = "Submit portfolio calculation task",
            notes = "Submits portfolio calculation task into the queue."
    )
    @RequestMapping(
            value = "/",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskInfo> submitTask(
            @Valid
            @RequestBody AllocationRequest request
    ) {
        TaskInfo result = recommendationService.submitTask(request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Receive task status"
    )
    @RequestMapping(
            value = "/status/{taskId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TaskInfo> getTaskStatus(
            @NotEmpty
            @PathVariable("taskId")  String taskId
    ) throws APIException {
        TaskInfo result = recommendationService.getTaskInfo(taskId);
        if (result == null) {
            throw new APIException(String.format("Task '%s' not found", taskId));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Receive task result",
            notes = "Returns recommendation to bring desired money allocation."
    )
    @RequestMapping(
            value = "/result/{taskId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AllocationResult> getTaskResult(
            @NotEmpty
            @PathVariable("taskId") String taskId
    ) throws APIException {
        AllocationResult result = recommendationService.getTaskResult(taskId);
        if (result == null) {
            throw new APIException(String.format("Task '%s' not found", taskId));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Return tasks statuses"
    )
    @RequestMapping(
            value = "/stats",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<TaskStatus, Long>> getStats() {
        Map<TaskStatus, Long> result = recommendationService.getStats();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
