package com.nibado.example.saga.workflow;

import com.nibado.example.saga.mock.AsyncService;
import com.nibado.example.saga.mock.TopicFactory;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.client.ActivityCompletionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class ItemOrderActivitiesAsyncImpl implements ItemOrderActivitiesAsync {
    private static final Logger log = LoggerFactory.getLogger(ItemOrderActivitiesAsyncImpl.class);

    private final ActivityCompletionClient completionClient;
    private final AsyncService asyncService;

    public ItemOrderActivitiesAsyncImpl(ActivityCompletionClient completionClient, AsyncService asyncService) {
        this.completionClient = completionClient;
        this.asyncService = asyncService;

        asyncService.subscribeCostEvent(this::consumeCostEvent);
        asyncService.subscribeCreditEvent(this::consumeCreditEvent);
    }

    @Override
    public int calculateCost(List<ItemQtty> items) {
        log.info("Calculating cost for {} items", items.size());
        ActivityExecutionContext context = Activity.getExecutionContext();

        var token = Base64.getEncoder().encodeToString(context.getTaskToken());
        asyncService.calculateCost(token, items);

        context.doNotCompleteOnReturn();

        return -1; //Return value is ignored
    }

    @Override
    public int creditReservation(String id, int amount) {
        return 0;
    }

    @Override
    public void compensateCreditReservation(int id) {
        //Fake this bit, normally it would be another message on a topic.
        log.info("Compensating credit reservation {}", id);
    }

    @Override
    public void sendReservedItems(String userId) {
        log.info("Sending reserved items for user {}", userId);
    }

    private void consumeCostEvent(TopicFactory.Message message) {
        var token = Base64.getDecoder().decode(message.id());

        String messageString = new String(message.data());

        //When succesful, cost calculation is an integer. When not, it's a non-integer error message.
        //Normally you'd have proper 'error' messages but this is just a demonstration :)

        try {
            var amount = Integer.parseInt(messageString);
            log.info("Cost for items: {}", amount);
            completionClient.complete(token, amount);
        } catch (Exception e) {
            log.error("Cost calculation returned {}", messageString);
            completionClient.completeExceptionally(token, new Exception(messageString));
        }
    }

    private void consumeCreditEvent(TopicFactory.Message message) {
        var token = token(message);

        String messageString = new String(message.data());

        //When succesful, cost calculation is an integer. When not, it's a non-integer error message.
        //Normally you'd have proper 'error' messages but this is just a demonstration :)

        System.out.println(messageString);
    }

    private static byte[] token(TopicFactory.Message message) {
        return Base64.getDecoder().decode(message.id());
    }
}
