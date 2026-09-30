package ru.mirea.project.service;

import ru.mirea.project.model.Owner;
import ru.mirea.project.repository.OwnerRepository;
import ru.mirea.project.repository.PetRepository;
import ru.mirea.project.repository.BookingRepository;
import ru.mirea.project.model.Pet;
import ru.mirea.project.model.Species;
import ru.mirea.project.model.EnclosureSize;
import ru.mirea.project.exception.BusinessRuleException;
import java.util.List;

public class PetService {
    private final PetRepository petRepo;
    private final OwnerRepository ownerRepo;
    private final BookingRepository bookingRepo;

    public PetService(PetRepository petRepo, OwnerRepository ownerRepo, BookingRepository bookingRepo) {
        this.petRepo = petRepo;
        this.ownerRepo = ownerRepo;
        this.bookingRepo = bookingRepo;
    }

    private void validatePetData(Long ownerId, String name, Species species, EnclosureSize size) {
        if (ownerId == null) {
            throw new BusinessRuleException("ID владельца не может быть пустым");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Имя питомца не может быть пустым");
        }
        if (name.length() > 20) {
            throw new BusinessRuleException("Имя питомца не может быть длиннее 20 символов");
        }
        if (!name.matches("\\p{L}+")) {
            throw new BusinessRuleException("Имя питомца должно содержать только буквы");
        }
        if (species == null) {
            throw new BusinessRuleException("Вид питомца не может быть пустым");
        }
        if (size == null) {
            throw new BusinessRuleException("Размер питомца не может быть пустым");
        }
    }
    public Pet getById(Long petId) {
        return petRepo.findById(petId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Питомец не найден"
                ));
    }
    public List<Pet> getByOwnerId(Long ownerId) {
        checkOwnerExists(ownerId);
        return petRepo.findByOwnerId(ownerId);
    }

    private void checkOwnerExists(Long ownerId) {
        ownerRepo.findById(ownerId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Владелец не найден"
                ));
    }


    private void checkPetExists(Long petId) {
        petRepo.findById(petId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Питомец не найден"
                ));
    }

    public Pet create(Long ownerId, String name, Species species, EnclosureSize size) {
        validatePetData(ownerId, name, species, size);
        checkOwnerExists(ownerId);
        Pet pet = new Pet(ownerId, name, species, size);
        return petRepo.save(pet);
    }

    public List<Pet> getAll(){
        return petRepo.findAll();
    }

    // Поиск по части имени, без учёта регистра: "бар" найдёт "Барсик"
    public List<Pet> searchByName(String part) {
        String lower = part.toLowerCase();
        return petRepo.findAll().stream()
                .filter(pet -> pet.getName().toLowerCase().contains(lower))
                .toList();
    }

    public Pet update(Long petId, Long ownerId, String name, Species species, EnclosureSize size) {
        validatePetData(ownerId, name, species, size);
        checkOwnerExists(ownerId);
        checkPetExists(petId);

        Pet updatedPet = new Pet(petId, ownerId, name, species, size);

        if(!petRepo.update(updatedPet)) {
            throw new BusinessRuleException("Ошибка при обновлении питомца");
        }
        return updatedPet;
    }

    public void delete(Long petId) {
        checkPetExists(petId);
        if (bookingRepo.existsByPetId(petId)) {
            throw new BusinessRuleException("Невозможно удалить питомца с существующими бронированиями");
        }
        if(!petRepo.deleteById(petId)) {
            throw new BusinessRuleException("Ошибка при удалении питомца");
        }
    }
}
