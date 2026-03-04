package cz.podzimek.rental;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RentalServiceTest {

    @Test
    void itemCannotBeRentedTwice() {

        RentalService service = new RentalService();

        service.rentItem("item1");

        assertThrows(IllegalStateException.class, () -> {
            service.rentItem("item1");
        });

    }
}