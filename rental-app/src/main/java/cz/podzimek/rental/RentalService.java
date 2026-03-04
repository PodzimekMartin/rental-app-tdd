package cz.podzimek.rental;

import java.util.HashSet;
import java.util.Set;

public class RentalService {

    private Set<String> rentedItems = new HashSet<>();

    public void rentItem(String itemId) {

        if (rentedItems.contains(itemId)) {
            throw new IllegalStateException("Item already rented");
        }

        rentedItems.add(itemId);
    }
}