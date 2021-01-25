package com.nibado.example.saga.workflow;

import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface ItemOrderActivitiesAsync {
    int calculateCost(List<ItemQtty> items);

    int creditReservation(String id, int amount);

    void compensateCreditReservation(int id);

    void sendReservedItems(String userId);
}
