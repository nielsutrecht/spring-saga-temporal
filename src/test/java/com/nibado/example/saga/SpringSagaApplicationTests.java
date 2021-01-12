package com.nibado.example.saga;

import com.nibado.example.saga.mock.CreditClient;
import com.nibado.example.saga.mock.StockClient;
import com.nibado.example.saga.mock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringSagaApplicationTests {

    @Autowired
    private RestTemplate template;

    @Autowired
    private StockService stockService;

    @LocalServerPort
    private int port;

    private CreditClient creditClient;
    private StockClient stockClient;

    @BeforeEach
    public void setup() {
        stockService.fillStock();
        creditClient = new CreditClient("http://localhost:" + port + "/credit", template);
        stockClient = new StockClient("http://localhost:" + port + "/stock", template);
    }

    @Test
    void canMakeCreditReservation() {
        var reservation1 = creditClient.createReservation("john", 500);
        assertThat(reservation1.amountLeft()).isEqualTo(500);
        var reservation2 = creditClient.createReservation("john", 500);
        assertThat(reservation2.amountLeft()).isEqualTo(0);
    }

    @Test
    void canDeleteCreditReservation() {
        var reservation1 = creditClient.createReservation("jane", 500);
        creditClient.deleteReservation(reservation1.id());
    }

    @Test
    void notFoundOnDeleteCreditReservationThatDoesNotExist() {
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            creditClient.deleteReservation(-1);
        });
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void canMakeStockReservation() {
        var reservation1 = stockClient.createReservation("john", "Apple", 500);
        assertThat(reservation1.qtty()).isEqualTo(500);
        assertThat(reservation1.user()).isEqualTo("john");

        var reservation2 = stockClient.createReservation("jane", "Apple", 500);
        assertThat(reservation2.qtty()).isEqualTo(500);
        assertThat(reservation2.user()).isEqualTo("jane");
    }

    @Test
    void canCancelStockReservation() {
        var reservation1 = stockClient.createReservation("john", "Apple", 500);
        stockClient.deleteReservation(reservation1.id());
    }

    @Test
    void cantMakeStockReservationForUnavailableItem() {
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            stockClient.createReservation("john", "Banana", 500);
        });
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void cantMakeStockReservationForReservedItem() {
        stockClient.createReservation("john", "Apple", 1000);
        var ex = assertThrows(HttpClientErrorException.class, () -> {
            stockClient.createReservation("jane", "Apple", 1);
        });
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
