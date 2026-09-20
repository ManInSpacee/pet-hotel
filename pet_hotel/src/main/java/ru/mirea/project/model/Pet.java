package ru.mirea.project.model;

public class Pet {

    private final Long id;
    private final Long ownerId;
    private final String name;
    private final Species species;
    private final EnclosureSize size;

    public Pet(Long ownerId, String name, Species species, EnclosureSize size) {
        this(null, ownerId, name, species, size);
    }

    public Pet(Long id, Long ownerId, String name, Species species, EnclosureSize size) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.size = size;
    }

    @Override
    public String toString() {
        return id + ": " + name + " (" + species + ", " + size + ")";
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Species getSpecies() {
        return species;
    }

    public EnclosureSize getSize() {
        return size;
    }
}
