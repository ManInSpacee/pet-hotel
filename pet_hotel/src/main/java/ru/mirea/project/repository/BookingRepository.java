package ru.mirea.project.repository;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends Repository<Booking, Long> {
    List<Booking> findByOwnerId(Long ownerId);
    List<Booking> findOverlapping(Long enclosureId,  LocalDate from, LocalDate to);
    List<Booking> findByDateRange(LocalDate from, LocalDate to);
    List<Booking >findByStatus(BookingStatus status);
}
