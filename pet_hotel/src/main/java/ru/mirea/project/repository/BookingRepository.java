package ru.mirea.project.repository;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends Repository<Booking, Long> {
    List<Booking> findByOwnerId(Long ownerId);
    boolean hasOverlappingBookings(Long enclosureId,  LocalDate startDate, LocalDate endDate);
    List<Booking> findByDateRange(LocalDate from, LocalDate to);
    List<Booking >findByStatus(BookingStatus status);
}
