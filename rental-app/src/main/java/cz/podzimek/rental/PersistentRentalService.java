package cz.podzimek.rental;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PersistentRentalService {

    private static final int DAILY_PRICE = 100;
    private static final int LATE_FEE_PER_DAY = 200;

    private final JdbcItemRepository itemRepository;
    private final JdbcRentalRepository rentalRepository;
    private final Clock clock;

    public PersistentRentalService(
            JdbcItemRepository itemRepository,
            JdbcRentalRepository rentalRepository,
            Clock clock
    ) {
        this.itemRepository = itemRepository;
        this.rentalRepository = rentalRepository;
        this.clock = clock;
    }

    public void addItem(UserRole role, String itemId) {

        if (role != UserRole.ADMIN) {
            throw new IllegalStateException("Only ADMIN can add items");
        }

        if (itemRepository.exists(itemId)) {
            throw new IllegalStateException("Item already exists in catalog");
        }

        itemRepository.insert(itemId);
    }

    public void rentItemWithDueDate(String itemId, int days) {

        if (!itemRepository.exists(itemId)) {
            throw new IllegalStateException("Item not in catalog");
        }

        if (rentalRepository.hasActiveRental(itemId)) {
            throw new IllegalStateException("Item already rented");
        }

        LocalDate start = LocalDate.now(clock);
        LocalDate due = start.plusDays(days);

        rentalRepository.insertActiveRental(itemId, start, due);
    }

    public void returnItemOn(String itemId, LocalDate returnDate) {

        if (rentalRepository.getStatus(itemId) != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Item is not currently rented");
        }

        LocalDate startDate = rentalRepository.getStartDate(itemId);
        LocalDate dueDate = rentalRepository.getDueDate(itemId);

        // NOVÉ BUSINESS PRAVIDLO
        if (returnDate.isBefore(startDate)) {
            throw new IllegalStateException("Return date cannot be before start date");
        }

        int basePrice =
                (int) ChronoUnit.DAYS.between(startDate, dueDate)
                        * DAILY_PRICE;

        long lateDays =
                ChronoUnit.DAYS.between(dueDate, returnDate);

        int fee =
                lateDays > 0
                        ? (int) lateDays * LATE_FEE_PER_DAY
                        : 0;

        rentalRepository.markReturnedWithPrice(
                itemId,
                basePrice + fee
        );
    }

    public int getTotalPriceForItem(String itemId) {
        return rentalRepository.getTotalPrice(itemId);
    }
}