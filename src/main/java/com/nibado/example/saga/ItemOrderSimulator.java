package com.nibado.example.saga;

import com.nibado.example.saga.workflow.ItemOrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ItemOrderSimulator {
    private static final Logger log = LoggerFactory.getLogger(ItemOrderSimulator.class);
    private final WorkflowClient client;

    public ItemOrderSimulator(WorkflowClient client) {
        this.client = client;
    }

    @Scheduled(fixedRate = 1000L)
    public void scheduled() {
        var workFlow = client.newWorkflowStub(ItemOrderWorkflow.class, TemporalConfig.ITEM_ORDER_OPTIONS);

        try {
            workFlow.orderItems(Collections.emptyList());
        } catch (WorkflowException e) {
            log.error("Error in workflow: {}", e.getMessage(), e);
        }
    }
}
