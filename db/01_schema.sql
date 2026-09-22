CREATE TABLE enclosures (
    id BIGSERIAL PRIMARY KEY,
    number INT UNIQUE NOT NULL,
    size VARCHAR(10) NOT NULL CHECK ( size IN ('SMALL', 'MEDIUM', 'LARGE') )
);

CREATE TABLE owners (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL CONSTRAINT fk_pets_owner REFERENCES owners(id),
    name VARCHAR(20) NOT NULL,
    species VARCHAR(20) NOT NULL CHECK ( species in ('DOG', 'CAT', 'BIRD', 'RODENT') ),
    size VARCHAR(20) NOT NULL CHECK ( size IN ('SMALL', 'MEDIUM', 'LARGE') )
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL CONSTRAINT fk_bookings_pet REFERENCES pets(id),
    enclosure_id BIGINT NOT NULL CONSTRAINT fk_bookings_enclosure REFERENCES enclosures(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status in ('PENDING', 'ACCEPTED', 'DENIED', 'CANCELLED', 'COMPLETED')  ),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_bookings_dates CHECK (end_date - start_date >= 2)
);