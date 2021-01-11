package com.nibado.example.saga.workflow;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface ItemOrderActivities {
    void debug();
}
