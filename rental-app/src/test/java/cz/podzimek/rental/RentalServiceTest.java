package cz.podzimek.rental;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
}