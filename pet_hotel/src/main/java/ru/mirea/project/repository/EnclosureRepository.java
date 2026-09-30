package ru.mirea.project.repository;

import ru.mirea.project.model.Enclosure;

import java.util.Optional;


public interface EnclosureRepository extends Repository<Enclosure, Long> {
    Optional<Enclosure> findByNumber(int number);
}
