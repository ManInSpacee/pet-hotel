package ru.mirea.project.service;

import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.repository.EnclosureRepository;
import ru.mirea.project.repository.OwnerRepository;
import ru.mirea.project.repository.PetRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {
    private final OwnerRepository ownerRepo;
    private final PetRepository petRepo;
    private final EnclosureRepository enclosureRepo;
    private final BookingRepository bookingRepo;

    public StatisticsService(OwnerRepository ownerRepo, PetRepository petRepo,
                             EnclosureRepository enclosureRepo, BookingRepository bookingRepo) {
        this.ownerRepo = ownerRepo;
        this.petRepo = petRepo;
        this.enclosureRepo = enclosureRepo;
        this.bookingRepo = bookingRepo;
    }

    public Map<String, Long> collect() {
        List<Booking> bookings = bookingRepo.findAll();
        LocalDate today = LocalDate.now();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Владельцев", (long) ownerRepo.findAll().size());
        stats.put("Питомцев", (long) petRepo.findAll().size());
        stats.put("Вольеров", (long) enclosureRepo.findAll().size());
        stats.put("Бронирований всего", (long) bookings.size());
        for (BookingStatus status : BookingStatus.values()) {
            stats.put("  в статусе " + status, countByStatus(bookings, status));
        }
        stats.put("Сейчас в гостинице", bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .filter(b -> !today.isBefore(b.getStartDate()) && today.isBefore(b.getEndDate()))
                .count());
        return stats;
    }

    private long countByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream()
                .filter(b -> b.getStatus() == status)
                .count();
    }
}
