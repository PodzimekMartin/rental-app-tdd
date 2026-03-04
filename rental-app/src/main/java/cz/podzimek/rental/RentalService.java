package cz.podzimek.rental;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RentalService {

    private final Set<String> rentedItems = new HashSet<>();
    private final Map<String, Integer> activeRentalsByCustomer = new HashMap<>();

    public void rentItem(String itemId) {
        if (rentedItems.contains(itemId)) {
            throw new IllegalStateException("Item already rented");
        }
        rentedItems.add(itemId);
    }

    public void rentItemForCustomer(String customerId, String itemId) {
        int current = activeRentalsByCustomer.getOrDefault(customerId, 0);
        if (current >= 3) {
            throw new IllegalStateException("Customer already has 3 active rentals");
        }

        // použijeme už existující pravidlo: item nesmí být půjčený
        rentItem(itemId);

        activeRentalsByCustomer.put(customerId, current + 1);
    }
}