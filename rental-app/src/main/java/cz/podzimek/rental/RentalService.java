package cz.podzimek.rental;

import java.util.HashMap;
import java.util.Map;

public class RentalService {

    private final Map<String, RentalStatus> statusByItem = new HashMap<>();
    private final Map<String, Integer> activeRentalsByCustomer = new HashMap<>();

    public void rentItem(String itemId) {
        if (statusByItem.get(itemId) == RentalStatus.ACTIVE) {
            throw new IllegalStateException("Item already rented");
        }
        statusByItem.put(itemId, RentalStatus.ACTIVE);
    }

    public void rentItemForCustomer(String customerId, String itemId) {
        int current = activeRentalsByCustomer.getOrDefault(customerId, 0);
        if (current >= 3) {
            throw new IllegalStateException("Customer already has 3 active rentals");
        }

        rentItem(itemId);

        activeRentalsByCustomer.put(customerId, current + 1);
    }

    public void returnItem(String itemId) {
        if (statusByItem.get(itemId) != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Item is not currently rented");
        }
        statusByItem.put(itemId, RentalStatus.RETURNED);
    }
}