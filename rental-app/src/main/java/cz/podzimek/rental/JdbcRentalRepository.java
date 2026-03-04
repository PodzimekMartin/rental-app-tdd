package cz.podzimek.rental;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;

public class JdbcRentalRepository {

    private final DataSource dataSource;

    public JdbcRentalRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertActiveRental(String itemId, LocalDate startDate, LocalDate dueDate) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO rentals(item_id, status, start_date, due_date, total_price)
                 VALUES (?, ?, ?, ?, NULL)
             """)) {

            ps.setString(1, itemId);
            ps.setString(2, RentalStatus.ACTIVE.name());

            // JDBC DATE
            ps.setDate(3, Date.valueOf(startDate));
            ps.setDate(4, Date.valueOf(dueDate));

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasActiveRental(String itemId) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT status FROM rentals WHERE item_id = ?"
             )) {

            ps.setString(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                return RentalStatus.ACTIVE.name()
                        .equals(rs.getString("status"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RentalStatus getStatus(String itemId) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT status FROM rentals WHERE item_id = ?"
             )) {

            ps.setString(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalStateException("Rental not found");
                }

                return RentalStatus.valueOf(rs.getString("status"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void markReturnedWithPrice(String itemId, int totalPrice) {

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 UPDATE rentals
                 SET status = ?, total_price = ?
                 WHERE item_id = ?
             """)) {

            ps.setString(1, RentalStatus.RETURNED.name());
            ps.setInt(2, totalPrice);
            ps.setString(3, itemId);

            int updated = ps.executeUpdate();

            if (updated == 0) {
                throw new IllegalStateException("Rental not found");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalPrice(String itemId) {

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT total_price FROM rentals WHERE item_id = ?"
             )) {

            ps.setString(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalStateException("Rental not found");
                }

                Integer price = (Integer) rs.getObject("total_price");

                if (price == null) {
                    throw new IllegalStateException("Price not available");
                }

                return price;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDate getStartDate(String itemId) {

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT start_date FROM rentals WHERE item_id = ?"
             )) {

            ps.setString(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalStateException("Rental not found");
                }

                Date sqlDate = rs.getDate("start_date");
                if (sqlDate == null) {
                    throw new IllegalStateException("Start date not available");
                }

                return sqlDate.toLocalDate();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDate getDueDate(String itemId) {

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT due_date FROM rentals WHERE item_id = ?"
             )) {

            ps.setString(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalStateException("Rental not found");
                }

                Date sqlDate = rs.getDate("due_date");
                if (sqlDate == null) {
                    throw new IllegalStateException("Due date not available");
                }

                return sqlDate.toLocalDate();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}