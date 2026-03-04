package cz.podzimek.rental;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RentalService {

    // itemId -> stav půjčky (true = aktivní půjčeno, false = vráceno)
    private final Map<String, Boolean> rentalActiveByItem = new HashMap<>();

    // kolik aktivních půjček má zákazník
    private final Map<String, Integer> activeRentalsByCustomer = new HashMap<>();

    public void rentItem(String itemId) {
        // item už je aktivně půjčený?
        if (Boolean.TRUE.equals(rentalActiveByItem.get(itemId))) {
            throw new IllegalStateException("Item already rented");
        }
        rentalActiveByItem.put(itemId, true);
    }

    public void rentItemForCustomer(String customerId, String itemId) {
        int current = activeRentalsByCustomer.getOrDefault(customerId, 0);
        if (current >= 3) {
            throw new IllegalStateException("Customer already has 3 active rentals");
        }

        rentItem(itemId); // pravidlo: item nejde půjčit dvakrát

        activeRentalsByCustomer.put(customerId, current + 1);
    }

    public void returnItem(String itemId) {
        // pokud není aktivně půjčený, nejde vrátit (vráceno už bylo / nikdy půjčeno nebylo)
        if (!Boolean.TRUE.equals(rentalActiveByItem.get(itemId))) {
            throw new IllegalStateException("Item is not currently rented");
        }
        rentalActiveByItem.put(itemId, false);
    }
}