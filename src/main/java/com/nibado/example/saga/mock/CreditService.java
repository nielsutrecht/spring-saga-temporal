package com.nibado.example.saga.mock;

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
    private static final int MAX_CREDIT = 1000;
    private final List<CreditReservation> reservations = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger();

    @PostMapping("/reservation")
    public ResponseEntity<CreditReservationResponse> reserveCredit(@RequestBody CreditReservationRequest req) {
        var creditAvailable = MAX_CREDIT - reservations.stream()
            .filter(r -> r.userId().equals(req.id()))
            .mapToInt(CreditReservation::amount)
            .sum();

        if (creditAvailable < req.amount()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            var reservation = new CreditReservation(nextId.getAndIncrement(), req.id(), req.amount());
            reservations.add(reservation);

            return ResponseEntity.ok(new CreditReservationResponse(
                reservation.id(),
                reservation.amount(),
                creditAvailable - reservation.amount()));
        }
    }

    @DeleteMapping("/reservation/{id}")
    public void deleteCreditReservation(@PathVariable int id) {
        reservations.removeIf(r -> r.id() == id);
    }

    private record CreditReservation(int id, String userId, int amount) {
    }

    private record CreditReservationRequest(String id, int amount) {
    }

    private record CreditReservationResponse(int id, int amount, int amountLeft) {
    }
}

