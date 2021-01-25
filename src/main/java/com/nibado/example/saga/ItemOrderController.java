package com.nibado.example.saga;

import com.nibado.example.saga.config.TemporalConfig;
import com.nibado.example.saga.workflow.ItemOrderWorkflow;
import com.nibado.example.saga.workflow.ItemOrderWorkflowAsync;
import com.nibado.example.saga.workflow.ItemQtty;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowException;
import io.temporal.failure.ActivityFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class ItemOrderController {
    private static final Logger log = LoggerFactory.getLogger(ItemOrderController.class);
    private final WorkflowClient client;

    public ItemOrderController(WorkflowClient client) {
        this.client = client;
    }

    @PostMapping
    public String orderItems(@RequestBody OrderRequest request) {
        try {
            var workFlow = client.newWorkflowStub(ItemOrderWorkflow.class, TemporalConfig.ITEM_ORDER_OPTIONS);
            workFlow.orderItems(request.userId, request.items);

            return "SUCCESS";
        } catch (WorkflowException e) {
            var activityFailure = (ActivityFailure) e.getCause();

            log.error("Error in workflow {}: {}", e.getWorkflowType().get(), activityFailure.getActivityType());
            return "FAILED";
        }
    }

    @PostMapping("/async")
    public void orderItemsAsync(@RequestBody OrderRequest request) {

        var workFlow = client.newWorkflowStub(ItemOrderWorkflowAsync.class, TemporalConfig.ITEM_ORDER_OPTIONS_ASYNC);
        workFlow.orderItems(request.userId, request.items);

        var future = WorkflowClient.execute(workFlow::orderItems, request.userId, request.items);

        future.whenComplete(this::asyncOrderComplete);
    }

    private void asyncOrderComplete(String result, Throwable exception) {
        log.info("Result: {}, exception: {}", result, exception);
    }

    private record OrderRequest(String userId, List<ItemQtty> items) {
    }
}
