package com.nibado.example.saga.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface ItemOrderWorkflow {
    @WorkflowMethod
    void orderItems(List<ItemQtty> items);
}
