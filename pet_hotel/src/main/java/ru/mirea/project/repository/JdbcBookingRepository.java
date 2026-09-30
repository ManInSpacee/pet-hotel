package ru.mirea.project.repository;

import ru.mirea.project.exception.DataAccessException;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.BookingStatus;
import ru.mirea.project.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookingRepository implements BookingRepository {


    private final static String FIND_BY_OWNER_ID = "SELECT b.id, b.pet_id, b.enclosure_id, b.start_date, b.end_date, b.status, b.created_at " + "FROM bookings b " + "JOIN pets p ON p.id = b.pet_id " + "WHERE p.owner_id = ?";
    private final static String FIND_OVERLAPPING_BOOKINGS = "SELECT EXISTS (SELECT 1 FROM bookings WHERE enclosure_id = ? AND status IN ('PENDING', 'ACCEPTED') AND end_date >= ? AND start_date <= ?)";
    private final static String FIND_ALL = "SELECT id, pet_id, enclosure_id, start_date, end_date, status, created_at FROM bookings";
    private final static String SAVE = "INSERT INTO bookings (pet_id, enclosure_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";
    private final static String UPDATE = "UPDATE bookings SET pet_id = ?, enclosure_id = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
    private final static String FIND_BY_ID = "SELECT id, pet_id, enclosure_id, start_date, end_date, status, created_at FROM bookings WHERE id = ?";
    private final static String DELETE_BY_ID = "DELETE FROM bookings WHERE id = ?";
    private final static String FIND_BY_STATUS = "SELECT id, pet_id, enclosure_id, start_date, end_date, status, created_at FROM bookings WHERE status = ?";
    private final static String FIND_BY_DATE_RANGE = "SELECT id, pet_id, enclosure_id, start_date, end_date, status, created_at FROM bookings WHERE end_date >= ? AND start_date <= ?";
    private final static String EXISTS_BY_ENCLOSURE_ID = "SELECT EXISTS (SELECT 1 FROM bookings WHERE enclosure_id = ?)";

    @Override
    public List<Booking> findByOwnerId(Long ownerId) {

        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_OWNER_ID);) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения бронирований по id владельца: " + e.getMessage(), e);
        }
        return bookings;
    }

    @Override
    public boolean hasOverlappingBookings(Long enclosureId, LocalDate startDate, LocalDate endDate) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_OVERLAPPING_BOOKINGS);) {
            ps.setLong(1, enclosureId);
            ps.setDate(2, java.sql.Date.valueOf(startDate));
            ps.setDate(3, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка проверки пересекающихся бронирований: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<Booking> findByDateRange(LocalDate from, LocalDate to) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_BY_DATE_RANGE);
        ) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении бронирований по заданным датам: " + e.getMessage(), e);
        }
        return bookings;
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_BY_STATUS);
        ) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти бронирования по статусу: " + e.getMessage(), e);
        }
        return bookings;
    }

    @Override
    public Booking save(Booking entity) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SAVE, new String[]{"id", "created_at"});) {
            bindParams(entity, ps);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    LocalDateTime createdAt = keys.getTimestamp(2).toLocalDateTime();
                    return new Booking(id, entity.getPetId(), entity.getEnclosureId(), entity.getStartDate(), entity.getEndDate(), entity.getStatus(), createdAt);
                }
            }
            throw new SQLException("База не вернула сгенерированный id");
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось создать бронирование: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Booking entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Невозможно обновить бронирование без id");
        }
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(UPDATE);

        ) {
            ps.setLong(6, entity.getId());
            bindParams(entity, ps);
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Не удалось обновить бронирование: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Booking> findById(Long bookingId) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_ID);) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти бронирование: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findAll() {

        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_ALL);) {
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти бронирования: " + e.getMessage(), e);
        }
        return bookings;
    }

    @Override
    public boolean existsByEnclosureId(Long enclosureId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_BY_ENCLOSURE_ID);
        ) {
            ps.setLong(1, enclosureId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при проверке существования брони по id вольера: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean deleteById(Long bookingId) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID);) {
            ps.setLong(1, bookingId);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить бронирование: " + e.getMessage(), e);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long petId = rs.getLong("pet_id");
        Long enclosureId = rs.getLong("enclosure_id");
        LocalDate startDate = rs.getDate("start_date").toLocalDate();
        LocalDate endDate = rs.getDate("end_date").toLocalDate();
        BookingStatus status = BookingStatus.valueOf(rs.getString("status"));
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return new Booking(id, petId, enclosureId, startDate, endDate, status, createdAt);
    }

    private void bindParams(Booking entity, PreparedStatement ps) throws SQLException {
        ps.setLong(1, entity.getPetId());
        ps.setLong(2, entity.getEnclosureId());
        ps.setDate(3, java.sql.Date.valueOf(entity.getStartDate()));
        ps.setDate(4, java.sql.Date.valueOf(entity.getEndDate()));
        ps.setString(5, entity.getStatus().name());
    }
}
