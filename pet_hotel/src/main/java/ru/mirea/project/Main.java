package ru.mirea.project;

import ru.mirea.project.model.EnclosureSize;
import ru.mirea.project.repository.EnclosureRepository;
import ru.mirea.project.repository.JdbcEnclosureRepository;
import ru.mirea.project.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        System.out.println("pet-hotel");


        //        Это просто подключение к базе данных, чтобы проверить, что она работает
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("подключено к " + conn.getCatalog());
        } catch (SQLException e) {
            System.out.println("ошибка подключения: " + e.getMessage());
        }

        //      Тут мы создаем репозиторий для работы с вольерами и выполняем несколько операций, чтобы проверить его работу
        EnclosureRepository repository = new JdbcEnclosureRepository();

        //      Создание вольеров
        System.out.println("Создание вольера: " + repository.save(new ru.mirea.project.model.Enclosure(null, 1, EnclosureSize.SMALL)));
        System.out.println("Создание вольера: " + repository.save(new ru.mirea.project.model.Enclosure(null, 2, EnclosureSize.MEDIUM)));
        System.out.println("Создание вольера: " + repository.save(new ru.mirea.project.model.Enclosure(null, 3, EnclosureSize.LARGE)));

        //      Отображение всех вольеров
        System.out.println("Все вольеры:" + repository.findAll());

        //      Поиск вольера по id
        System.out.println("Вольер с id=1: " + repository.findById(1L));

        //      Пример обновления вольера с id=1
        System.out.println("Обновление вольера с id=1: " + repository.update(new ru.mirea.project.model.Enclosure(1L, 10, EnclosureSize.LARGE)));
        System.out.println("Вольер с id=1 после обновления: " + repository.findById(1L));
        //      Пример удаления вольера с id=2
        System.out.println("Удаление вольера с id=2: " + repository.deleteById(2L));
        System.out.println("Поиск удаленного вольера: " + repository.findById(2L));
        System.out.println("Все вольеры после удаления: " + repository.findAll());
    }
}
