package com.nibado.example.saga.mock;

import org.springframework.web.client.RestTemplate;

public class CreditClient {
    private final String baseUrl;
    private final RestTemplate template;

    public CreditClient(String baseUrl, RestTemplate template) {
        this.template = template;
        this.baseUrl = baseUrl;
    }

    public CreditService.CreditReservationResponse createReservation(String id, int amount) {
        var request = new CreditService.CreditReservationRequest(id, amount);
        var entity = template.postForEntity(baseUrl + "/reservation", request, CreditService.CreditReservationResponse.class);

        return entity.getBody();
    }

    public void deleteReservation(int id) {
        template.delete(baseUrl + "/reservation/{userId}", id);
    }
}
