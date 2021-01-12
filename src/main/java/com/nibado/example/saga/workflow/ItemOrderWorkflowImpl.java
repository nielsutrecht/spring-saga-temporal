package com.nibado.example.saga.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

public class ItemOrderWorkflowImpl implements ItemOrderWorkflow {
    private final ActivityOptions options =
        ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofHours(1))
            // disable retries for example to run faster
            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
            .build();

    private final ItemOrderActivities activities =
        Workflow.newActivityStub(ItemOrderActivities.class, options);

    @Override
    public void orderItems(String userId, List<ItemQtty> items) {
        Saga.Options sagaOptions = new Saga.Options.Builder()
            .setParallelCompensation(true).build();
        Saga saga = new Saga(sagaOptions);

        try {
            var creditRequired = activities.calculateCost(items);

            var creditReservation = activities.creditReservation(userId, creditRequired);
            saga.addCompensation(activities::compensateCreditReservation, creditReservation);

            for (var item : items) {
                var stockReservation = activities.stockReservation(userId, item.name(), item.qtty());
                saga.addCompensation(activities::compensateStockReservation, stockReservation);
            }

            activities.sendReservedItems(userId);

        } catch (ActivityFailure e) {
            saga.compensate();
            throw e;
        }
    }
}
