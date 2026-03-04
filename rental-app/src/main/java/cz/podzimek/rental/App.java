package cz.podzimek.rental;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Scanner;

public class App {

    public static void main(String[] args) throws Exception {

        DataSource ds = createDataSource();
        initSchema(ds);

        JdbcItemRepository itemRepo = new JdbcItemRepository(ds);
        JdbcRentalRepository rentalRepo = new JdbcRentalRepository(ds);

        PersistentRentalService service =
                new PersistentRentalService(itemRepo, rentalRepo, Clock.systemDefaultZone());

        Scanner scanner = new Scanner(System.in);

        System.out.println("Select role:");
        System.out.println("1 - ADMIN");
        System.out.println("2 - CUSTOMER");

        int roleChoice = scanner.nextInt();
        scanner.nextLine();

        UserRole role = roleChoice == 1 ? UserRole.ADMIN : UserRole.CUSTOMER;

        while (true) {

            System.out.println();
            System.out.println("Rental System");
            System.out.println("1 - Add item");
            System.out.println("2 - Rent item");
            System.out.println("3 - Return item");
            System.out.println("4 - Show price");
            System.out.println("0 - Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                break;
            }

            try {

                switch (choice) {

                    case 1 -> {

                        System.out.print("Item id: ");
                        String itemId = scanner.nextLine();

                        service.addItem(role, itemId);

                        System.out.println("Item added.");
                    }

                    case 2 -> {

                        System.out.print("Item id: ");
                        String itemId = scanner.nextLine();

                        System.out.print("Days: ");
                        int days = scanner.nextInt();
                        scanner.nextLine();

                        service.rentItemWithDueDate(itemId, days);

                        System.out.println("Item rented.");
                    }

                    case 3 -> {

                        System.out.print("Item id: ");
                        String itemId = scanner.nextLine();

                        System.out.print("Return date (YYYY-MM-DD): ");
                        String date = scanner.nextLine();

                        service.returnItemOn(itemId, LocalDate.parse(date));

                        System.out.println("Item returned.");
                    }

                    case 4 -> {

                        System.out.print("Item id: ");
                        String itemId = scanner.nextLine();

                        int price = service.getTotalPriceForItem(itemId);

                        System.out.println("Total price: " + price);
                    }

                    default -> System.out.println("Unknown option");

                }

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());

            }

        }

        System.out.println("Application finished");
    }

    private static DataSource createDataSource() {

        JdbcDataSource ds = new JdbcDataSource();

        ds.setURL("jdbc:h2:mem:appdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");

        return ds;
    }

    private static void initSchema(DataSource ds) throws Exception {

        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {

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
                    total_price INT
                )
            """);

        }
    }
}