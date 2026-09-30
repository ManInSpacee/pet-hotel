package ru.mirea.project.repository;

import ru.mirea.project.model.Owner;
import ru.mirea.project.util.DatabaseManager;
import ru.mirea.project.exception.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcOwnerRepository implements OwnerRepository {

    private static final String FIND_ALL = "SELECT id, login, full_name, phone FROM owners";
    private static final String SAVE = "INSERT INTO owners (login, full_name, phone) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID = "SELECT id, login, full_name, phone FROM owners WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM owners WHERE id = ?";
    private static final String UPDATE = "UPDATE owners SET login = ?, full_name = ?, phone = ? WHERE id = ?";
    private static final String FIND_BY_PHONE = "SELECT id, login, full_name, phone FROM owners WHERE phone = ?";
    private static final String FIND_BY_LOGIN = "SELECT id, login, full_name, phone FROM owners WHERE login = ?";

    // метод сохранения оунера в базу
    @Override
    public Owner save(Owner entity){
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(SAVE, PreparedStatement.RETURN_GENERATED_KEYS);){

            ps.setString(1, entity.getLogin());
            ps.setString(2, entity.getFullName());
            ps.setString(3, entity.getPhone());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()){
                    Long id = keys.getLong(1);
                    return new Owner(id, entity.getLogin(), entity.getFullName(), entity.getPhone());
                }
            }
            throw new SQLException("База не вернула сгенерированный id");
        } catch (SQLException e){
            throw new DataAccessException("Ошибка сохранения владельца в базу данных: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Owner> findByLogin(String login) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_LOGIN)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения владельца по логину: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Owner> findByPhone(String phone) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_PHONE);) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения владельца по телефону: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Owner> findById(Long ownerId) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_BY_ID);) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения владельца по id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Owner> findAll() {
        List<Owner> owners = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(FIND_ALL); ResultSet rs = ps.executeQuery()){
            while(rs.next()) {
                owners.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения всех владельцев: " + e.getMessage(), e);
        }
        return owners;
    }

    @Override
    public boolean deleteById(Long ownerId) {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID)) {
            ps.setLong(1, ownerId);
            int deleted = ps.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Ошибка удаления владельца по id: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Owner entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Невозможно обновить владельца без id");
        }
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(UPDATE);) {
            ps.setString(1, entity.getLogin());
            ps.setString(2, entity.getFullName());
            ps.setString(3, entity.getPhone());
            ps.setLong(4, entity.getId());
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка обновления владельца: " + e.getMessage(), e);
        }
    }
    private Owner mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String login = rs.getString("login");
        String fullName = rs.getString("full_name");
        String phone = rs.getString("phone");
        return new Owner(id, login, fullName, phone);
    }
}