package cz.podzimek.rental;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RentalServiceTest {

    @Test
    void itemCannotBeRentedTwice() {
        RentalService service = new RentalService();

        service.rentItem("item1");

        assertThrows(IllegalStateException.class, () -> service.rentItem("item1"));
    }

    @Test
    void customerCannotHaveMoreThanThreeActiveRentals() {
        RentalService service = new RentalService();

        service.rentItemForCustomer("customer1", "item1");
        service.rentItemForCustomer("customer1", "item2");
        service.rentItemForCustomer("customer1", "item3");

        assertThrows(IllegalStateException.class, () ->
                service.rentItemForCustomer("customer1", "item4")
        );
    }

    @Test
    void rentalCannotBeReturnedTwice() {
        RentalService service = new RentalService();

        service.rentItem("item1");
        service.returnItem("item1");

        assertThrows(IllegalStateException.class, () ->
                service.returnItem("item1")
        );
    }

    @Test
    void calculatesPriceWithLateFee() {
        // "dnešek" nastavíme natvrdo, aby test byl stabilní
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-04T00:00:00Z"), ZoneOffset.UTC);

        RentalService service = new RentalService(fixedClock);

        // půjčím na 3 dny, základ = 100 Kč/den => 300 Kč
        // vrátím o 2 dny později, pokuta = 200 Kč/den => 400 Kč
        service.rentItemWithDueDate("item1", 3);

        service.returnItemOn("item1", LocalDate.parse("2026-03-09")); // 2 dny po termínu (termín 2026-03-07)

        int price = service.getTotalPriceForItem("item1");

        assertEquals(700, price);
    }

    @Test
    void onlyAdminCanAddNewItem() {
        RentalService service = new RentalService();

        assertThrows(IllegalStateException.class, () ->
                service.addItem("CUSTOMER", "item42")
        );

        // admin může
        service.addItem("ADMIN", "item42");
    }
}