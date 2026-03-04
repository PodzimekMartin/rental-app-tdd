package cz.podzimek.rental;

import java.time.LocalDate;

public class RentalDetails {
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private RentalStatus status;
    private Integer totalPrice;

    public RentalDetails(LocalDate startDate, LocalDate dueDate) {
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = RentalStatus.ACTIVE;
        this.totalPrice = null;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public RentalStatus getStatus() { return status; }
    public void setStatus(RentalStatus status) { this.status = status; }
    public Integer getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
}