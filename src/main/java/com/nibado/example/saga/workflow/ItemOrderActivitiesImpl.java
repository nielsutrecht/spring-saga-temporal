package com.nibado.example.saga.workflow;

import com.nibado.example.saga.mock.CreditClient;
import com.nibado.example.saga.mock.StockClient;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

public class ItemOrderActivitiesImpl implements ItemOrderActivities {
    private static final Logger log = LoggerFactory.getLogger(ItemOrderActivitiesImpl.class);

    private final CreditClient creditClient;
    private final StockClient stockClient;

    public ItemOrderActivitiesImpl(CreditClient creditClient, StockClient stockClient) {
        this.creditClient = creditClient;
        this.stockClient = stockClient;
    }

    @Override
    public int calculateCost(List<ItemQtty> items) {
        log.info("Calculating cost for {} items", items.size());

        try {
            return CostCalculator.calculate(items);
        } catch (IllegalArgumentException e) {
            throw Workflow.wrap(e);
        }
    }

    @Override
    public int creditReservation(String userId, int amount) {
        log.info("Reserving {} credit for user {}", amount, userId);
        try {
            return creditClient.createReservation(userId, amount).id();
        } catch (HttpClientErrorException e) {
            throw Workflow.wrap(e);
        }
    }

    @Override
    public void compensateCreditReservation(int id) {
        log.info("Compensating credit reservation {}", id);
        try {
            creditClient.deleteReservation(id);
        } catch (HttpClientErrorException e) {
            throw Workflow.wrap(e);
        }
    }

    @Override
    public int stockReservation(String userId, String item, int amount) {
        log.info("Reserving {} {} for user {}", amount, item, userId);
        try {
            return stockClient.createReservation(userId, item, amount).id();
        } catch (HttpClientErrorException e) {
            throw Workflow.wrap(e);
        }
    }

    @Override
    public void compensateStockReservation(int id) {
        log.info("Compensating stock reservation {}", id);
        try {
            stockClient.deleteReservation(id);
        } catch (HttpClientErrorException e) {
            throw Workflow.wrap(e);
        }
    }

    @Override
    public void sendReservedItems(String userId) {
        log.info("Sending reserved items for user {}", userId);
        try {
            stockClient.sendReservedItems(userId);
        } catch (HttpClientErrorException e) {
            throw Workflow.wrap(e);
        }
    }
}
