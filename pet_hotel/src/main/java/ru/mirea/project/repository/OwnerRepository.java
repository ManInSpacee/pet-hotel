package ru.mirea.project.repository;

import ru.mirea.project.model.Owner;

import java.util.Optional;

public interface OwnerRepository extends Repository<Owner, Long>
{
    Optional<Owner> findByPhone(String phone);
}
