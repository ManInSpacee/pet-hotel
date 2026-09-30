package ru.mirea.project.ui;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.exception.DataAccessException;
import ru.mirea.project.model.Owner;
import ru.mirea.project.service.BookingService;
import ru.mirea.project.service.EnclosureService;
import ru.mirea.project.service.OwnerService;
import ru.mirea.project.service.PetService;

import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConsoleMenu {

    private static final String MAIN_MENU_TEXT = """
             === ГОСТИНИЦА ДЛЯ ЖИВОТНЫХ ===
             1. Владельцы
             2. Питомцы
             3. Вольеры
             4. Бронирования
             5. Поиск
             6. Фильтрация
             7. Сортировка
             8. Статистика
             9. Экспорт в Excel
             0. Выход
            """;
    private static final String OWNER_MENU_TEXT = """
             === ГОСТИНИЦА ДЛЯ ЖИВОТНЫХ === Владельцы ===
             1. Добавить владельца
             2. Редактировать владельца
             3. Удалить владельца
             4. Список владельцев
             5. Найти владельца по ID
             0. Выход
            """;
    private final BookingService bookingService;
    private final OwnerService ownerService;
    private final PetService petService;
    private final EnclosureService enclosureService;
    private final Scanner scanner = new Scanner(System.in);

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    private int readInt(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }
    }
    private Long readLong(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ID должен содержать только числа в диапазонe от 0 до " + Long.MAX_VALUE);
            }
        }
    }

    private String readValid(String prompt, Consumer<String> check) {
        while (true) {
            try {
                String input = readString(prompt);
                check.accept(input);
                return input;
            } catch (BusinessRuleException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public ConsoleMenu(BookingService bookingService, OwnerService ownerService, PetService petService, EnclosureService enclosureService) {
        this.bookingService = bookingService;
        this.ownerService = ownerService;
        this.petService = petService;
        this.enclosureService = enclosureService;
    }

    private void addOwner() {
        String login = readValid("Введите login: ", ownerService::validateLogin);
        String fullName = readValid("Введите полное имя: ", ownerService::validateFullName);
        String phone = readValid("Введите номер телефона: ", ownerService::validatePhone);
        try {
            System.out.println("Создан: " + ownerService.create(login, fullName, phone));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void editOwner() {
        Long id = readLong("Введите ID владельца: ");
        try {
            Owner owner = ownerService.getById(id);
            System.out.println("Редактируем: " + owner);
            String login = readValid("Введите новый login: ", ownerService::validateLogin);
            String fullName = readValid("Введите новое полное имя: ", ownerService::validateFullName);
            String phone = readValid("Введите новый номер телефона: ", ownerService::validatePhone);

            System.out.println("Обновлено: " + ownerService.update(id, login, fullName, phone));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteOwner() {
        Long id = readLong("Введите ID владельца: ");
        try {
            ownerService.delete(id);
            System.out.println("Владелец удалён");
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listOwners() {
        List<Owner> owners = ownerService.getAll();
        if (owners.isEmpty()) {
            System.out.println("Владельцев пока нет");
            return;
        }
        owners.forEach(System.out::println);
    }

    private void findOwner() {
        Long id = readLong("Введите ID владельца: ");
        try {
            System.out.println(ownerService.getById(id));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void ownersMenu() {
        while (true) {
            System.out.println(OWNER_MENU_TEXT);
            int choice = readInt("Введите номер пункта меню: ");

            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> addOwner();
                case 2 -> editOwner();
                case 3 -> deleteOwner();
                case 4 -> listOwners();
                case 5 -> findOwner();
                default -> System.out.println("Нет такого пункта");
            }
        }
    }



    public void run() {
        while (true) {
            System.out.println(MAIN_MENU_TEXT);
            int choice = readInt("Введите номер пункта меню: ");
            try {
                switch (choice) {
                    case 0 -> {
                        return;
                    }
                    case 1 -> ownersMenu();
                }
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }


        }
    }


}
