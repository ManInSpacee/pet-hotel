package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.model.Enclosure;
import ru.mirea.project.model.EnclosureSize;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.repository.EnclosureRepository;

import java.util.List;
import java.util.Optional;

public class EnclosureService {

    private final BookingRepository bookingRepo;
    private final EnclosureRepository enclosureRepo;

    public EnclosureService(BookingRepository bookingRepo, EnclosureRepository enclosureRepo) {
        this.bookingRepo = bookingRepo;
        this.enclosureRepo = enclosureRepo;
    }

    private void validateData(int enclosureNumber, EnclosureSize size) {
        if (enclosureNumber <= 0) {
            throw new BusinessRuleException("Номер вольера должен быть положительным числом");
        }
        if (size == null) {
            throw new BusinessRuleException("Размер вольера не может быть пустым");
        }
    }

    public Enclosure create(int number, EnclosureSize size) {
        validateData(number, size);
        if (enclosureRepo.findByNumber(number).isPresent()) {
            throw new BusinessRuleException("Вольер с номером " + number + " уже существует");
        }

        return enclosureRepo.save(new Enclosure(number, size));
    }

    public Enclosure update(Long enclosureId, int newNumber, EnclosureSize newSize) {
        validateData(newNumber, newSize);
        Optional<Enclosure> sameNumber = enclosureRepo.findByNumber(newNumber);
        if (sameNumber.isPresent() && !sameNumber.get().getId().equals(enclosureId)) {
            throw new BusinessRuleException("Указанный номер " + newNumber + " уже используется");
        }

        Enclosure updatedEnclosure = new Enclosure(enclosureId, newNumber, newSize);
        if (!enclosureRepo.update(updatedEnclosure)) {
            throw new BusinessRuleException("Вольер с id " + enclosureId + " не найден");
        }

        return updatedEnclosure;
    }

    public void delete(Long enclosureId) {
        if (enclosureRepo.findById(enclosureId).isEmpty()) {
            throw new BusinessRuleException("Вольер для удаления не найден");
        }
        if (bookingRepo.existsByEnclosureId(enclosureId)) {
            throw new BusinessRuleException("Невозможно удалить вольер с существующими бронированиями");
        }
        enclosureRepo.deleteById(enclosureId);
    }

    public List<Enclosure> findAll() {
        return enclosureRepo.findAll();
    }

}
