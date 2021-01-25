package com.nibado.example.saga.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface ItemOrderWorkflowAsync {
    @WorkflowMethod
    String orderItems(String userId, List<ItemQtty> items);
}
