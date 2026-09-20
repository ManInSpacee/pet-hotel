package ru.mirea.project.model;

public class Enclosure {
    private final Long id;
    private final int number;
    private final EnclosureSize size;

    public Enclosure(int number, EnclosureSize size) {
        this(null, number, size);
    }
    public Enclosure(Long id, int number, EnclosureSize size) {
        this.id = id;
        this.number = number;
        this.size = size;
    }

    @Override
    public String toString() {
        return id + ": " + number + " " + size;
    }

    public boolean canHost(EnclosureSize petSize) {
        return this.size.compareTo(petSize) >= 0;
    }

    public int getNumber() {
        return number;
    }

    public EnclosureSize getSize() {
        return size;
    }

    public Long getId() {
        return id;
    }
}
