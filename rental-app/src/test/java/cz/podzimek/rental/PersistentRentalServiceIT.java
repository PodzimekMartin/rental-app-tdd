package cz.podzimek.rental;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class PersistentRentalServiceIT {

    @Test
    void persistsItemAndRentalAndCalculatesPrice() throws Exception {

        DataSource ds = createDataSource();
        initSchema(ds);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-03-04T00:00:00Z"),
                ZoneOffset.UTC
        );

        JdbcItemRepository itemRepo = new JdbcItemRepository(ds);
        JdbcRentalRepository rentalRepo = new JdbcRentalRepository(ds);

        PersistentRentalService service =
                new PersistentRentalService(itemRepo, rentalRepo, fixedClock);

        service.addItem(UserRole.ADMIN, "item1");

        assertTrue(itemRepo.exists("item1"));

        service.rentItemWithDueDate("item1", 3);
        service.returnItemOn("item1", LocalDate.parse("2026-03-09"));

        int total = service.getTotalPriceForItem("item1");

        assertEquals(700, total);

        assertEquals(RentalStatus.RETURNED, rentalRepo.getStatus("item1"));
    }

    @Test
    void cannotReturnBeforeStartDate() throws Exception {

        DataSource ds = createDataSource();
        initSchema(ds);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-03-04T00:00:00Z"),
                ZoneOffset.UTC
        );

        JdbcItemRepository itemRepo = new JdbcItemRepository(ds);
        JdbcRentalRepository rentalRepo = new JdbcRentalRepository(ds);

        PersistentRentalService service =
                new PersistentRentalService(itemRepo, rentalRepo, fixedClock);

        service.addItem(UserRole.ADMIN, "itemX");
        service.rentItemWithDueDate("itemX", 5);

        assertThrows(IllegalStateException.class, () ->
                service.returnItemOn("itemX", LocalDate.parse("2020-04-04"))
        );
    }

    private static DataSource createDataSource() {

        JdbcDataSource ds = new JdbcDataSource();

        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");

        return ds;
    }

    private static void initSchema(DataSource ds) throws Exception {

        try (Connection c = ds.getConnection();
             Statement st = c.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id VARCHAR(100) PRIMARY KEY
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS rentals (
                    item_id VARCHAR(100) PRIMARY KEY,
                    status VARCHAR(20),
                    start_date DATE,
                    due_date DATE,
                    total_price INT,
                    FOREIGN KEY (item_id) REFERENCES items(id)
                )
            """);
        }
    }
    @Test
    void customerCannotHaveMoreThanThreeActiveRentals() throws Exception {

        DataSource ds = createDataSource();
        initSchema(ds);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-03-04T00:00:00Z"),
                ZoneOffset.UTC
        );

        JdbcItemRepository itemRepo = new JdbcItemRepository(ds);
        JdbcRentalRepository rentalRepo = new JdbcRentalRepository(ds);

        PersistentRentalService service =
                new PersistentRentalService(itemRepo, rentalRepo, fixedClock);

        service.addItem(UserRole.ADMIN, "item1");
        service.addItem(UserRole.ADMIN, "item2");
        service.addItem(UserRole.ADMIN, "item3");
        service.addItem(UserRole.ADMIN, "item4");

        service.rentItemWithDueDate("CUSTOMER1", "item1", 5);
        service.rentItemWithDueDate("CUSTOMER1", "item2", 5);
        service.rentItemWithDueDate("CUSTOMER1", "item3", 5);

        assertThrows(IllegalStateException.class, () ->
                service.rentItemWithDueDate("CUSTOMER1", "item4", 5)
        );
    }
}