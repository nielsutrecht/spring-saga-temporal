package com.nibado.example.saga.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a Mock service that represents a 'credit' service where users can 'reserve' credit up to 1000 dollars similar
 * to credit reservations on credit cards.
 */

@RestController
@RequestMapping("/credit")
public class CreditService {
    private static final Logger log = LoggerFactory.getLogger(CreditService.class);
    private static final int MAX_CREDIT = 1000;
    private final List<CreditReservation> reservations = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger();

    @PostMapping("/reservation")
    public ResponseEntity<CreditReservationResponse> reserveCredit(@RequestBody CreditReservationRequest req) {
        var creditAvailable = MAX_CREDIT - reservations.stream()
            .filter(r -> r.userId().equals(req.userId()))
            .mapToInt(CreditReservation::amount)
            .sum();

        if (creditAvailable < req.amount()) {
            log.error("Not enough credit available for user {}", req.userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            var reservation = new CreditReservation(nextId.getAndIncrement(), req.userId, req.amount);
            reservations.add(reservation);

            log.info("Credit reservation {} for {} added for user {}, available: {}",
                reservation.id,
                reservation.amount,
                reservation.userId,
                creditAvailable - reservation.amount());

            return ResponseEntity.ok(new CreditReservationResponse(
                reservation.id,
                reservation.amount,
                creditAvailable - reservation.amount()));
        }
    }

    @DeleteMapping("/reservation/{id}")
    public ResponseEntity<Void> deleteCreditReservation(@PathVariable int id) {
        if(reservations.stream().noneMatch(r -> r.id == id)) {
            log.error("No reservation with userId {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        reservations.removeIf(r -> r.id == id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    public record CreditReservation(int id, String userId, int amount) {
    }

    public record CreditReservationRequest(String userId, int amount) {
    }

    public record CreditReservationResponse(int id, int amount, int amountLeft) {
    }
}

