package ru.mirea.project.repository;
import ru.mirea.project.model.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends Repository<Booking, Long> {
    Booking update(Booking booking);
    Optional<Booking> findByOwnerId(Long id);
    List<Booking> findOverlapping(Long enclosureId,  LocalDate from, LocalDate to);
    List<Booking> findByDateRange(LocalDate from, LocalDate to);
}
