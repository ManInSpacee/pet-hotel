package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.model.Owner;
import ru.mirea.project.repository.OwnerRepository;

import java.util.List;

public class OwnerService {
    private final OwnerRepository ownerRepo;

    public OwnerService (OwnerRepository ownerRepo) {
        this.ownerRepo = ownerRepo;
    }

    private void validateOwnerData(String login, String fullName, String phone) {
        if (login == null || login.isBlank()) {
            throw new BusinessRuleException("Логин не может быть пустым");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new BusinessRuleException("Имя владельца не может быть пустым");
        }

        if (phone == null || phone.isBlank()) {
            throw new BusinessRuleException("Телефон не может быть пустым");
        }

        if (login.length() > 20) {
            throw new BusinessRuleException("Логин не может быть длиннее 20 символов");
        }

        if (fullName.length() > 50) {
            throw new BusinessRuleException("Имя не может быть длиннее 50 символов");
        }

        if (phone.length() > 20) {
            throw new BusinessRuleException("Телефон не может быть длиннее 20 символов");
        }
    }

    public Owner create(String login, String fullName, String phone) {
        validateOwnerData(login, fullName, phone);
        if (ownerRepo.findByPhone(phone).isPresent()) {
            throw new BusinessRuleException("Владелец с таким телефоном уже существует");
        }
        Owner owner = new Owner(login, fullName, phone);
        return ownerRepo.save(owner);
    }

    public Owner getById(Long ownerId) {
        return ownerRepo.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Владелец не найден"
                ));
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

        Owner updatedOwner = new Owner(ownerId, login, fullName, phone);
        boolean updated = ownerRepo.update(updatedOwner);
        if(!updated) {
            throw new BusinessRuleException("Ошибка при обновлении владельца");
        }
        return updatedOwner;
    }
    public void delete(Long ownerId) {
        getById(ownerId);
        boolean deleted = ownerRepo.deleteById(ownerId);
        if (!deleted) {
            throw new BusinessRuleException("Ошибка при удалении владельца");
        }
    }


}
