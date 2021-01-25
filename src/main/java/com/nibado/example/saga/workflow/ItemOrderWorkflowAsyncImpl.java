package com.nibado.example.saga.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

public class ItemOrderWorkflowAsyncImpl implements ItemOrderWorkflowAsync {
    private final ActivityOptions options =
        ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofHours(1))
            // disable retries for example to run faster
            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
            .build();

    private final ItemOrderActivitiesAsync activities =
        Workflow.newActivityStub(ItemOrderActivitiesAsync.class, options);

    @Override
    public String orderItems(String userId, List<ItemQtty> items) {
        Saga.Options sagaOptions = new Saga.Options.Builder()
            .setParallelCompensation(true).build();
        Saga saga = new Saga(sagaOptions);

        try {
            var creditRequired = activities.calculateCost(items);

            var creditReservation = activities.creditReservation(userId, creditRequired);
            saga.addCompensation(activities::compensateCreditReservation, creditReservation);

            activities.sendReservedItems(userId);

            return "SUCCESS";

        } catch (ActivityFailure e) {
            saga.compensate();
            throw e;
        }
    }
}
