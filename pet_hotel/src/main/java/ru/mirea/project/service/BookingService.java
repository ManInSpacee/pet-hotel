package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.model.*;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.repository.EnclosureRepository;
import ru.mirea.project.repository.PetRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingRepository bookingRepo;
    private final EnclosureRepository enclosureRepo;
    private final PetRepository petRepo;
    private static final int MIN_DAYS = 2;
    private static final int MAX_DAYS = 30;

    private void validateDates(LocalDate start, LocalDate end) {
        if (start.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Дата начала не может быть в прошлом");
        }
        if (ChronoUnit.DAYS.between(start, end) < MIN_DAYS) {
            throw new BusinessRuleException("Бронирование должно быть не менее " + MIN_DAYS + " дней");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_DAYS) {
            throw new BusinessRuleException("Бронирование должно быть не более " + MAX_DAYS + " дней");
        }
    }

    public BookingService(BookingRepository bookingRepo, EnclosureRepository enclosureRepo, PetRepository petRepo) {
        this.bookingRepo = bookingRepo;
        this.enclosureRepo = enclosureRepo;
        this.petRepo = petRepo;
    }

    public Booking accept(Long bookingId) {
        return changeStatus(bookingId, BookingStatus.ACCEPTED);
    }
    public Booking deny(Long bookingId) {
        return changeStatus(bookingId, BookingStatus.DENIED);
    }

    public Booking complete(Long bookingId) {
        return changeStatus(bookingId, BookingStatus.COMPLETED);
    }

    public Booking cancel(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new BusinessRuleException("Бронирование не найдено"));
        if (booking.getStatus() == BookingStatus.ACCEPTED && !LocalDate.now().isBefore(booking.getStartDate())) {
            throw new BusinessRuleException("Невозможно отменить бронирование, которое уже началось");
        }
        return changeStatus(bookingId, BookingStatus.CANCELLED);
    }

    public List<Enclosure> getAvailableEnclosures(Long petId, LocalDate start, LocalDate end) {

        validateDates(start, end);

        Pet pet = petRepo.findById(petId).orElseThrow(() -> new BusinessRuleException("Питомец не найден"));
        List<Enclosure> allEnclosures = enclosureRepo.findAll();
        return allEnclosures.stream()
                .filter(enc -> enc.canHost(pet.getSize()))
                .filter(enc -> !bookingRepo.hasOverlappingBookings(enc.getId(), start, end))
                .collect(Collectors.toList());
    }

    private Booking changeStatus(Long bookingId, BookingStatus newStatus) {

        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new BusinessRuleException("Бронирование не найдено"));

        if (!booking.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessRuleException("Невозможно изменить статус бронирования из " + booking.getStatus() + " в " + newStatus);
        }

        Booking updatedBooking = new Booking(booking.getId(), booking.getPetId(), booking.getEnclosureId(), booking.getStartDate(), booking.getEndDate(), newStatus, booking.getCreatedAt());
        bookingRepo.update(updatedBooking);
        return updatedBooking;
    }

    public Booking create(Long petId, Long enclosureId, LocalDate start, LocalDate end) {
        validateDates(start, end);
        Pet pet = petRepo.findById(petId).orElseThrow(() -> new BusinessRuleException("Питомец не найден"));
        Enclosure enclosure = enclosureRepo.findById(enclosureId).orElseThrow(() -> new BusinessRuleException("Вольер не найден"));
        if (!enclosure.canHost(pet.getSize())) {
            throw new BusinessRuleException("Вольер не подходит для размера питомца");
        }
        if (bookingRepo.hasOverlappingBookings(enclosure.getId(), start, end)) {
            throw new BusinessRuleException("Вольер уже забронирован на указанный период");
        }
        return bookingRepo.save(new Booking (petId, enclosureId, start, end));
    }

}
