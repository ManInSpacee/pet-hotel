package ru.mirea.project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Booking {
    private final Long id;
    private final Long petId;
    private final Long enclosureId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BookingStatus status;
    private final LocalDateTime createdAt;

    public Booking(Long petId, Long enclosureId, LocalDate startDate, LocalDate endDate) {
        this(null, petId, enclosureId, startDate, endDate, BookingStatus.PENDING, null);
    }

    public Booking(Long id, Long petId, Long enclosureId, LocalDate startDate, LocalDate endDate, BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.petId = petId;
        this.enclosureId = enclosureId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return id + ": " + petId + " " + enclosureId + " " + startDate + " " + endDate + " " + status + " " + createdAt;
    }

    public long durationDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public Long getId() {
        return id;
    }

    public Long getPetId() {
        return petId;
    }

    public Long getEnclosureId() {
        return enclosureId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
