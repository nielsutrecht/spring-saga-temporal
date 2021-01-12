package com.nibado.example.saga.mock;

import org.springframework.web.client.RestTemplate;

public class StockClient {
    private final String baseUrl;
    private final RestTemplate template;

    public StockClient(String baseUrl, RestTemplate template) {
        this.template = template;
        this.baseUrl = baseUrl;
    }

    public StockService.ItemReservation createReservation(String userId, String item, int amount) {
        var request = new StockService.ReservationRequest(userId, item, amount);
        var entity = template.postForEntity(baseUrl + "/reservation", request, StockService.ItemReservation.class);

        return entity.getBody();
    }

    public void deleteReservation(int id) {
        try {
            template.delete(baseUrl + "/reservation/{id}", id);
        } catch (Exception e) {
            throw e;
        }
    }

    public void sendReservedItems(String userId) {
        var entity = template.postForEntity(baseUrl + "/reservation/{userId}/send", null, Void.class, userId);
        entity.getBody();
    }
}
