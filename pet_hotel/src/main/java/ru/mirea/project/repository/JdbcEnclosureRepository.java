package ru.mirea.project.repository;

import ru.mirea.project.exception.DataAccessException;
import ru.mirea.project.model.Enclosure;
import ru.mirea.project.model.EnclosureSize;
import ru.mirea.project.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEnclosureRepository implements EnclosureRepository {

    private static final String FIND_ALL = "SELECT id, number, size FROM enclosures";
    private static final String SAVE = "INSERT INTO enclosures (number, size) VALUES (?, ?)";
    private static final String FIND_BY_ID = "SELECT id, number, size FROM enclosures WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM enclosures WHERE id = ?";
    private static final String UPDATE = "UPDATE enclosures SET number = ?, size = ? WHERE id = ?";

    @Override
    public Enclosure save(Enclosure entity) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SAVE, PreparedStatement.RETURN_GENERATED_KEYS);) {


            ps.setInt(1, entity.getNumber());
            ps.setString(2, entity.getSize().name());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    Enclosure enclosure = new Enclosure(id, entity.getNumber(), entity.getSize());
                    System.out.println("Сохранен вольер: " + enclosure);
                    return enclosure;
                }
            }
            throw new SQLException("База не вернула сгенерированный id");
        } catch (SQLException e) {
            throw new DataAccessException(("Ошибка сохранения вольера в базу данных: " + e.getMessage()), e);
        }

    }

    @Override
    public boolean update(Enclosure entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Невозможно обновить вольер без id");
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE);) {
            ps.setInt(1, entity.getNumber());
            ps.setString(2, entity.getSize().name());
            ps.setLong(3, entity.getId());
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка обновления вольера: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Enclosure> findById(Long enclosureId) {

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID);) {

            ps.setLong(1, enclosureId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска вольера по id: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Enclosure> findAll() {
        List<Enclosure> enclosures = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                enclosures.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения списка вольеров: " + e.getMessage(), e);
        }
        return enclosures;

    }

    @Override
    public boolean deleteById(Long enclosureId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID);) {
            ps.setLong(1, enclosureId);
            int deleted = ps.executeUpdate();

            return deleted > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка удаления вольера: " + e.getMessage(), e);
        }

    }

    private Enclosure mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        int number = rs.getInt("number");
        String size = rs.getString("size");
        return new Enclosure(id, number, EnclosureSize.valueOf(size));
    }
}
