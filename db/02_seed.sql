BEGIN;

INSERT INTO enclosures (id, number, size) VALUES
    (1, 101, 'SMALL'),
    (2, 102, 'MEDIUM'),
    (3, 103, 'LARGE'),
    (4, 201, 'SMALL'),
    (5, 202, 'MEDIUM'),
    (6, 203, 'LARGE')
ON CONFLICT (id) DO UPDATE SET
    number = EXCLUDED.number,
    size = EXCLUDED.size;

INSERT INTO owners (id, login, full_name, phone) VALUES
    (1, 'annas', 'Анна Смирнова', '+79001000101'),
    (2, 'borisk', 'Борис Кузнецов', '+79001000102'),
    (3, 'verap', 'Вера Петрова', '+79001000103'),
    (4, 'gennadyi', 'Геннадий Иванов', '+79001000104'),
    (5, 'dariav', 'Дарья Волкова', '+79001000105'),
    (6, 'egorm', 'Егор Морозов', '+79001000106'),
    (7, 'irinaf', 'Ирина Фёдорова', '+79001000107')
ON CONFLICT (id) DO UPDATE SET
    login = EXCLUDED.login,
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone;

INSERT INTO pets (id, owner_id, name, species, size) VALUES
    (1, 1, 'Рекс', 'DOG', 'LARGE'),
    (2, 1, 'Муся', 'CAT', 'SMALL'),
    (3, 2, 'Чарли', 'DOG', 'MEDIUM'),
    (4, 3, 'Кеша', 'BIRD', 'SMALL'),
    (5, 4, 'Пушок', 'CAT', 'MEDIUM'),
    (6, 5, 'Белка', 'RODENT', 'SMALL'),
    (7, 6, 'Грета', 'DOG', 'LARGE'),
    (8, 7, 'Тиша', 'CAT', 'SMALL'),
    (9, 2, 'Лаки', 'DOG', 'SMALL'),
    (10, 3, 'Ричи', 'BIRD', 'MEDIUM'),
    (11, 5, 'Марс', 'CAT', 'LARGE'),
    (12, 6, 'Пончик', 'RODENT', 'SMALL')
ON CONFLICT (id) DO UPDATE SET
    owner_id = EXCLUDED.owner_id,
    name = EXCLUDED.name,
    species = EXCLUDED.species,
    size = EXCLUDED.size;

INSERT INTO bookings (id, pet_id, enclosure_id, start_date, end_date, status, created_at) VALUES
    (1, 1, 3, '2026-09-29', '2026-10-05', 'ACCEPTED',  '2026-09-20 09:15:00'),
    (2, 2, 1, '2026-10-03', '2026-10-06', 'PENDING',   '2026-09-21 11:30:00'),
    (3, 3, 2, '2026-10-05', '2026-10-12', 'ACCEPTED',  '2026-09-22 14:05:00'),
    (4, 4, 4, '2026-09-10', '2026-09-14', 'COMPLETED', '2026-09-01 08:00:00'),
    (5, 5, 5, '2026-10-07', '2026-10-11', 'DENIED',    '2026-09-23 16:40:00'),
    (6, 6, 1, '2026-10-08', '2026-10-15', 'CANCELLED', '2026-09-24 10:20:00'),
    (7, 7, 6, '2026-10-10', '2026-10-18', 'PENDING',   '2026-09-25 12:10:00'),
    (8, 8, 4, '2026-09-18', '2026-09-22', 'COMPLETED', '2026-09-05 13:45:00'),
    (9, 9, 2, '2026-10-13', '2026-10-16', 'ACCEPTED',  '2026-09-26 09:00:00'),
    (10, 10, 5, '2026-10-14', '2026-10-20', 'PENDING', '2026-09-27 17:25:00'),
    (11, 11, 3, '2026-10-16', '2026-10-23', 'DENIED',  '2026-09-28 15:15:00'),
    (12, 12, 1, '2026-09-01', '2026-09-04', 'COMPLETED','2026-08-25 10:00:00'),
    (13, 1, 6, '2026-11-01', '2026-11-08', 'PENDING',  '2026-09-29 08:35:00'),
    (14, 5, 2, '2026-11-03', '2026-11-07', 'CANCELLED','2026-09-29 18:10:00')
ON CONFLICT (id) DO UPDATE SET
    pet_id = EXCLUDED.pet_id,
    enclosure_id = EXCLUDED.enclosure_id,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date,
    status = EXCLUDED.status,
    created_at = EXCLUDED.created_at;

SELECT setval(pg_get_serial_sequence('enclosures', 'id'), (SELECT MAX(id) FROM enclosures));
SELECT setval(pg_get_serial_sequence('owners', 'id'), (SELECT MAX(id) FROM owners));
SELECT setval(pg_get_serial_sequence('pets', 'id'), (SELECT MAX(id) FROM pets));
SELECT setval(pg_get_serial_sequence('bookings', 'id'), (SELECT MAX(id) FROM bookings));

COMMIT;
