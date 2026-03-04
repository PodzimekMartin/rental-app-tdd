package cz.podzimek.rental;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class RentalService {

    private static final int DAILY_PRICE = 100;
    private static final int LATE_FEE_PER_DAY = 200;

    private final Clock clock;

    private final Map<String, RentalStatus> statusByItem = new HashMap<>();
    private final Map<String, Integer> activeRentalsByCustomer = new HashMap<>();

    // nový detail půjčky kvůli ceně a termínu
    private final Map<String, RentalDetails> rentalByItem = new HashMap<>();

    // katalog itemů (ADMIN je může přidávat)
    private final Set<String> catalogItemIds = new HashSet<>();

    public RentalService() {
        this(Clock.systemDefaultZone());
    }

    public RentalService(Clock clock) {
        this.clock = clock;
    }

    // ====== NOVÉ FUNKCE PRO ROLE ======

    public void addItem(UserRole role, String itemId) {
        if (role != UserRole.ADMIN) {
            throw new IllegalStateException("Only ADMIN can add items");
        }

        // zabránění duplicitě v katalogu
        if (!catalogItemIds.add(itemId)) {
            throw new IllegalStateException("Item already exists in catalog");
        }
    }

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

    // ====== NOVÉ FUNKCE PRO CENU ======

    public void rentItemWithDueDate(String itemId, int days) {
        rentItem(itemId);

        LocalDate start = LocalDate.now(clock);
        LocalDate due = start.plusDays(days);

        rentalByItem.put(itemId, new RentalDetails(start, due));
    }

    public void returnItemOn(String itemId, LocalDate returnDate) {
        RentalDetails details = rentalByItem.get(itemId);
        if (details == null) {
            throw new IllegalStateException("No rental details for this item");
        }
        if (details.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Item already returned");
        }

        // základní cena = počet dní půjčky * 100
        int basePrice = (int) java.time.temporal.ChronoUnit.DAYS.between(details.getStartDate(), details.getDueDate()) * DAILY_PRICE;

        // zpoždění = returnDate - dueDate (jen pokud > 0)
        long lateDays = java.time.temporal.ChronoUnit.DAYS.between(details.getDueDate(), returnDate);
        int fee = lateDays > 0 ? (int) lateDays * LATE_FEE_PER_DAY : 0;

        details.setTotalPrice(basePrice + fee);
        details.setStatus(RentalStatus.RETURNED);

        // synchronizace i se stavem co už máš
        statusByItem.put(itemId, RentalStatus.RETURNED);
    }

    public int getTotalPriceForItem(String itemId) {
        RentalDetails details = rentalByItem.get(itemId);
        if (details == null || details.getTotalPrice() == null) {
            throw new IllegalStateException("Price not available");
        }
        return details.getTotalPrice();
    }
}