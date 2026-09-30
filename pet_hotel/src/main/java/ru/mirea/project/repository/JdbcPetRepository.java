package ru.mirea.project.repository;

import ru.mirea.project.model.EnclosureSize;
import ru.mirea.project.model.Pet;
import ru.mirea.project.model.Species;
import ru.mirea.project.util.DatabaseManager;
import ru.mirea.project.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPetRepository implements PetRepository {

    private static final String FIND_ALL = "SELECT id, owner_id, name, species, size FROM pets";
    private static final String FIND_BY_OWNER_ID = "SELECT id, owner_id, name, species, size FROM pets WHERE owner_id = ?";
    private static final String SAVE = "INSERT INTO pets (owner_id, name, species, size) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE pets SET owner_id = ?, name = ?, species = ?, size = ? WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT id, owner_id, name, species, size FROM pets WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM pets WHERE id = ?";

    @Override
    public List<Pet> findByOwnerId(Long ownerId) {
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_OWNER_ID);) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pets.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения питомцев по id владельца: " + e.getMessage(), e);
        } return pets;
    }

    @Override
    public Pet save(Pet entity) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SAVE, PreparedStatement.RETURN_GENERATED_KEYS);) {
            ps.setLong(1, entity.getOwnerId());
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getSpecies().name());
            ps.setString(4, entity.getSize().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    return new Pet(id, entity.getOwnerId(), entity.getName(), entity.getSpecies(), entity.getSize());
                }
            }
            throw new SQLException("База не вернула сгенерированный id");
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка сохранения питомца в базу данных: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Pet entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Невозможно обновить питомца без id");
        }
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setLong(1, entity.getOwnerId());
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getSpecies().name());
            ps.setString(4, entity.getSize().name());
            ps.setLong(5, entity.getId());
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Ошибка обновления питомца: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Pet> findById(Long id) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения питомца по id: " + e.getMessage(), e);
        } return Optional.empty();
    }

    @Override
    public List<Pet> findAll() {
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_ALL); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                pets.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения всех питомцев: " + e.getMessage(), e);
        } return pets;
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID);) {
            ps.setLong(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка удаления питомца по id: " + e.getMessage(), e);
        }
    }

    private Pet mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long ownerId = rs.getLong("owner_id");
        String name = rs.getString("name");
        Species species = Species.valueOf(rs.getString("species"));
        EnclosureSize size = EnclosureSize.valueOf(rs.getString("size"));
        return new Pet(id, ownerId, name, species, size);
    }
}


