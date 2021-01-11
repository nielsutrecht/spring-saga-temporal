package com.nibado.example.saga.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 * This is a Mock service that represents a 'stock' service where users can 'reserve' items one by one, and then can have
 * these items be sent to them, simulating situations where scenario's might have to be 'rolled back'.
 */

@RestController
@RequestMapping("/stock")
public class StockService {
    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final Map<String, AtomicInteger> warehouse = new HashMap<>();
    private final List<ItemReservation> reservations = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger();

    @PostMapping("/reservation")
    public ResponseEntity<ItemReservation> createReservation(@RequestBody ReservationRequest request) {
        synchronized (warehouse) {
            var reserved = reservations.stream()
                .filter(r -> r.item().equals(request.item))
                .mapToInt(r -> r.qtty)
                .sum();

            var available = warehouse
                .computeIfAbsent(request.item, k -> new AtomicInteger())
                .get() - reserved;

            if (available >= request.qtty) {
                var newReservation = new ItemReservation(nextId.getAndIncrement(), request.user, request.item, request.qtty);
                reservations.add(newReservation);

                log.info("Reserved {} {} for {}", request.qtty, request.item, request.user);

                return ResponseEntity.ok(newReservation);
            } else {
                log.info("Only {} available for item {}", available, request.item);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
    }

    @DeleteMapping("/reservation/{id")
    public ResponseEntity<Void> cancelReservations(int id) {
        synchronized (warehouse) {
            reservations.removeIf(r -> r.id == id);
            log.info("Removed reservation {}", id);
        }

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reservation/{user}/send")
    public ResponseEntity<Void> sendItems(@PathVariable String user) {
        synchronized (warehouse) {
            var userReservations = reservations.stream()
                .filter(r -> r.user.equals(user))
                .collect(toList());

            userReservations.forEach(r -> warehouse.get(r.item).addAndGet(-r.qtty));

            log.info("Processed {} reservations for user {}", userReservations.size(), user);

            return ResponseEntity.accepted().build();
        }
    }

    private record ReservationRequest(String user, String item, int qtty) {
    }

    private record ItemReservation(int id, String user, String item, int qtty) {
    }
}
