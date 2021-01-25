package com.nibado.example.saga.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibado.example.saga.workflow.ItemQtty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/*
 Mock service that simulate an asynchronous process by war
 */

@Service
public class AsyncService {
    private static final Logger log = LoggerFactory.getLogger(AsyncService.class);

    private final TopicFactory factory;
    private final CreditClient creditClient;
    private final StockClient stockClient;
    private final ObjectMapper mapper;

    private static final String COST_TOPIC = "cost";
    private static final String CREDIT_REQUEST_TOPIC = "credit_request";
    private static final String CREDIT_RESPONSE_TOPIC = "credit_response";
    private static final String STOCK_REQUEST_TOPIC = "stock_request";
    private static final String STOCK_RESPONSE_TOPIC = "stock_response";


    public AsyncService(TopicFactory factory, StockClient stockClient, CreditClient creditClient, ObjectMapper mapper) {
        this.factory = factory;
        this.stockClient = stockClient;
        this.creditClient = creditClient;
        this.mapper = mapper;
        factory.subScribe(CREDIT_REQUEST_TOPIC, this::handleReserveCreditRequest);

    }

    public void calculateCost(String id, List<ItemQtty> items) {
        log.info("Calculate cost request for {} items", items.size());
        try {
            var result = CostCalculator.calculate(items);
            factory.getTopic(COST_TOPIC).write(id, Integer.toString(result).getBytes());
        } catch (Exception e) {

            factory.getTopic(COST_TOPIC).write(id, e.getMessage().getBytes());
        }
    }

    public void subscribeCostEvent(Consumer<TopicFactory.Message> consumer) {
        factory.subScribe(COST_TOPIC, consumer);
    }

    public void reserveCredit(String id, String userId, int amount) {
        log.info("Publishing reserve credit request for user {} with amount {} to {}", userId, amount, CREDIT_REQUEST_TOPIC);
        writeTopic(id, CREDIT_REQUEST_TOPIC, new CreditReservationRequest(userId, amount));
    }

    private void handleReserveCreditRequest(TopicFactory.Message message) {
        var request = fromBytes(message.data(), CreditReservationRequest.class);

        log.info("Received reserve credit request for user {} with amount {}", request.userId, request.amount);

        var result = creditClient.createReservation(request.userId, request.amount);

        log.info("Publishing reserve credit response with id {} for user {} to {}", result.id(), request.userId, CREDIT_RESPONSE_TOPIC);

        writeTopic(message.id(), CREDIT_RESPONSE_TOPIC, result);
    }

    public void subscribeCreditEvent(Consumer<TopicFactory.Message> consumer) {
        factory.subScribe(CREDIT_RESPONSE_TOPIC, consumer);
    }

    private record CreditReservationRequest(String userId, int amount) {}

    private void writeTopic(String id, String topic, Object value) {
        factory.getTopic(topic).write(id, toBytes(value));
    }

    private byte[] toBytes(Object o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromBytes(byte[] value, Class<T> clazz) {
        try {
            return mapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
