package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.model.Owner;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.repository.OwnerRepository;

import java.util.List;

public class OwnerService {
    private final OwnerRepository ownerRepo;
    private final BookingRepository bookingRepo;

    public OwnerService(OwnerRepository ownerRepo, BookingRepository bookingRepo) {
        this.ownerRepo = ownerRepo;
        this.bookingRepo = bookingRepo;
    }

    public void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new BusinessRuleException("Логин не может быть пустым");
        }
        if (!login.matches("\\p{L}{1,12}")) {
            throw new BusinessRuleException(
                    "Логин должен содержать только буквы без пробелов и быть не длиннее 12 символов");
        }
    }

    public void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessRuleException("Имя владельца не может быть пустым");
        }
        if (fullName.length() < 10) {
            throw new BusinessRuleException(
                    "Имя владельца должно содержать минимум 10 символов");
        }
        if (fullName.length() > 50) {
            throw new BusinessRuleException("Имя не может быть длиннее 50 символов");
        }
        if (!fullName.matches("\\p{L}+(?: \\p{L}+)*")) {
            throw new BusinessRuleException(
                    "Имя владельца должно содержать только буквы и пробелы между словами");
        }
    }

    public void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessRuleException("Телефон не может быть пустым");
        }
        if (!phone.matches("\\+?[0-9]{11,20}")) {
            throw new BusinessRuleException(
                    "Телефон должен содержать от 11 до 20 цифр, знак + допускается только в начале");
        }
    }

    private void validateOwnerData(String login, String fullName, String phone) {
        validateLogin(login);
        validateFullName(fullName);
        validatePhone(phone);
    }

    public Owner create(String login, String fullName, String phone) {
        validateOwnerData(login, fullName, phone);
        if (ownerRepo.findByLogin(login).isPresent()) {
            throw new BusinessRuleException("Владелец с таким логином уже существует");
        }
        if (ownerRepo.findByPhone(phone).isPresent()) {
            throw new BusinessRuleException("Владелец с таким телефоном уже существует");
        }
        Owner owner = new Owner(login, fullName, phone);
        return ownerRepo.save(owner);
    }

    public Owner getById(Long ownerId) {
        return ownerRepo.findById(ownerId).orElseThrow(() -> new BusinessRuleException("Владелец не найден"));
    }

    public Owner getByPhone(String phone) {
        return ownerRepo.findByPhone(phone).orElseThrow(() -> new BusinessRuleException("Владелец не найден"));
    }

    public List<Owner> getAll() {
        return ownerRepo.findAll();
    }

    public Owner update(Long ownerId, String login, String fullName, String phone) {
        validateOwnerData(login, fullName, phone);
        getById(ownerId);

        ownerRepo.findByLogin(login).ifPresent(owner -> {
            if (!owner.getId().equals(ownerId)) {
                throw new BusinessRuleException("Владелец с таким логином уже существует");
            }
        });
        ownerRepo.findByPhone(phone).ifPresent(owner -> {
            if (!owner.getId().equals(ownerId)) {
                throw new BusinessRuleException("Владелец с таким телефоном уже существует");
            }
        });

        Owner updatedOwner = new Owner(ownerId, login, fullName, phone);
        boolean updated = ownerRepo.update(updatedOwner);
        if(!updated) {
            throw new BusinessRuleException("Ошибка при обновлении владельца");
        }
        return updatedOwner;
    }
    public void delete(Long ownerId) {
        getById(ownerId);
        if (!bookingRepo.findByOwnerId(ownerId).isEmpty()) {
            throw new BusinessRuleException("Невозможно удалить владельца с существующими бронированиями");
        }
        boolean deleted = ownerRepo.deleteById(ownerId);
        if (!deleted) {
            throw new BusinessRuleException("Ошибка при удалении владельца");
        }
    }


}
