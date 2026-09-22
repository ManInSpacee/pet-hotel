package ru.mirea.project.repository;

import ru.mirea.project.model.Pet;

import java.util.List;

public interface PetRepository extends Repository<Pet, Long> {
    List<Pet> findByOwnerId(Long ownerId);
}
