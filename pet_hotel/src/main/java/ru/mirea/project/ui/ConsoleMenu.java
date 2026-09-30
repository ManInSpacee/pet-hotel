package ru.mirea.project.ui;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.exception.DataAccessException;
import ru.mirea.project.model.*;
import ru.mirea.project.service.*;
import ru.mirea.project.util.DatabaseExcelDump;
import ru.mirea.project.util.DatabaseManager;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

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
             === Владельцы ===
             1. Добавить владельца
             2. Редактировать владельца
             3. Удалить владельца
             4. Список владельцев
             5. Найти владельца по ID
             0. Назад
            """;
    private static final String PET_MENU_TEXT = """
             === Питомцы ===
             1. Добавить питомца
             2. Удалить питомца
             3. Список питомцев
             4. Питомцы владельца
             0. Назад
            """;
    private static final String ENCLOSURE_MENU_TEXT = """
             === Вольеры ===
             1. Добавить вольер
             2. Редактировать вольер
             3. Удалить вольер
             4. Список вольеров
             0. Назад
            """;
    private static final String BOOKING_MENU_TEXT = """
             === Бронирования ===
             1. Создать бронирование
             2. Список бронирований
             3. Найти бронирование по ID
             4. Одобрить заявку
             5. Отклонить заявку
             6. Отменить бронирование
             7. Завершить бронирование
             8. Удалить бронирование
             0. Назад
            """;
    private static final String SEARCH_MENU_TEXT = """
             === Поиск ===
             1. Бронирования владельца
             2. Питомцы по имени
             0. Назад
            """;
    private static final String FILTER_MENU_TEXT = """
             === Фильтрация бронирований ===
             1. По статусу
             2. По периоду дат
             0. Назад
            """;
    private static final String SORT_MENU_TEXT = """
             === Сортировка бронирований ===
             1. По дате заезда
             2. По дате создания (новые сверху)
             0. Назад
            """;

    private final BookingService bookingService;
    private final OwnerService ownerService;
    private final PetService petService;
    private final EnclosureService enclosureService;
    private final StatisticsService statisticsService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(BookingService bookingService, OwnerService ownerService, PetService petService,
                       EnclosureService enclosureService, StatisticsService statisticsService) {
        this.bookingService = bookingService;
        this.ownerService = ownerService;
        this.petService = petService;
        this.enclosureService = enclosureService;
        this.statisticsService = statisticsService;
    }


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

    private LocalDate readDate(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: дата в формате ГГГГ-ММ-ДД, например " + LocalDate.now().plusDays(1));
            }
        }
    }

    private EnclosureSize readSize(String prompt) {
        while (true) {
            String input = readString(prompt + "[SMALL, MEDIUM, LARGE]: ");
            try {
                return EnclosureSize.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: выберите одно из значений списка");
            }
        }
    }

    private Species readSpecies(String prompt) {
        while (true) {
            String input = readString(prompt + "[CAT, DOG, RODENT, BIRD]: ");
            try {
                return Species.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: выберите одно из значений списка");
            }
        }
    }

    private BookingStatus readStatus(String prompt) {
        while (true) {
            String input = readString(prompt + "[PENDING, ACCEPTED, DENIED, CANCELLED, COMPLETED]: ");
            try {
                return BookingStatus.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: выберите одно из значений списка");
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

    private void printList(List<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        items.forEach(System.out::println);
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
                    case 2 -> petsMenu();
                    case 3 -> enclosuresMenu();
                    case 4 -> bookingsMenu();
                    case 5 -> searchMenu();
                    case 6 -> filterMenu();
                    case 7 -> sortMenu();
                    case 8 -> showStatistics();
                    case 9 -> exportToExcel();
                    default -> System.out.println("Нет такого пункта");
                }
            } catch (DataAccessException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
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
                case 4 -> printList(ownerService.getAll(), "Владельцев пока нет");
                case 5 -> findOwner();
                default -> System.out.println("Нет такого пункта");
            }
        }
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

    private void findOwner() {
        Long id = readLong("Введите ID владельца: ");
        try {
            System.out.println(ownerService.getById(id));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }


    private void petsMenu() {
        while (true) {
            System.out.println(PET_MENU_TEXT);
            int choice = readInt("Введите номер пункта меню: ");
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> addPet();
                case 2 -> deletePet();
                case 3 -> printList(petService.getAll(), "Питомцев пока нет");
                case 4 -> petsOfOwner();
                default -> System.out.println("Нет такого пункта");
            }
        }
    }

    private void addPet() {
        Long ownerId = readLong("ID владельца: ");
        String name = readString("Имя питомца: ");
        Species species = readSpecies("Вид ");
        EnclosureSize size = readSize("Размер ");
        try {
            System.out.println("Создан: " + petService.create(ownerId, name, species, size));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deletePet() {
        Long id = readLong("ID питомца: ");
        try {
            petService.delete(id);
            System.out.println("Питомец удалён");
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void petsOfOwner() {
        Long ownerId = readLong("ID владельца: ");
        try {
            printList(petService.getByOwnerId(ownerId), "У владельца нет питомцев");
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }


    private void enclosuresMenu() {
        while (true) {
            System.out.println(ENCLOSURE_MENU_TEXT);
            int choice = readInt("Введите номер пункта меню: ");
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> addEnclosure();
                case 2 -> editEnclosure();
                case 3 -> deleteEnclosure();
                case 4 -> printList(enclosureService.findAll(), "Вольеров пока нет");
                default -> System.out.println("Нет такого пункта");
            }
        }
    }

    private void addEnclosure() {
        int number = readInt("Номер вольера: ");
        EnclosureSize size = readSize("Размер ");
        try {
            System.out.println("Создан: " + enclosureService.create(number, size));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void editEnclosure() {
        Long id = readLong("ID вольера: ");
        int number = readInt("Новый номер: ");
        EnclosureSize size = readSize("Новый размер ");
        try {
            System.out.println("Обновлено: " + enclosureService.update(id, number, size));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteEnclosure() {
        Long id = readLong("ID вольера: ");
        try {
            enclosureService.delete(id);
            System.out.println("Вольер удалён");
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }


    private void bookingsMenu() {
        while (true) {
            System.out.println(BOOKING_MENU_TEXT);
            int choice = readInt("Введите номер пункта меню: ");
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> createBooking();
                case 2 -> printList(bookingService.getAll(), "Бронирований пока нет");
                case 3 -> runWithBookingId(bookingService::getById);
                case 4 -> runWithBookingId(bookingService::accept);
                case 5 -> runWithBookingId(bookingService::deny);
                case 6 -> runWithBookingId(bookingService::cancel);
                case 7 -> runWithBookingId(bookingService::complete);
                case 8 -> deleteBooking();
                default -> System.out.println("Нет такого пункта");
            }
        }
    }

    private void createBooking() {
        Long petId = readLong("ID питомца: ");
        LocalDate start = readDate("Дата заезда (ГГГГ-ММ-ДД): ");
        LocalDate end = readDate("Дата выезда (ГГГГ-ММ-ДД): ");
        try {
            List<Enclosure> available = bookingService.getAvailableEnclosures(petId, start, end);
            if (available.isEmpty()) {
                System.out.println("На эти даты подходящих вольеров нет, попробуйте другие даты");
                return;
            }
            System.out.println("Свободные вольеры:");
            available.forEach(System.out::println);
            Long enclosureId = readLong("ID выбранного вольера: ");
            System.out.println("Создано: " + bookingService.create(petId, enclosureId, start, end));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void runWithBookingId(Function<Long, Booking> action) {
        Long id = readLong("ID бронирования: ");
        try {
            System.out.println(action.apply(id));
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteBooking() {
        Long id = readLong("ID бронирования: ");
        try {
            bookingService.delete(id);
            System.out.println("Бронирование удалено");
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }


    private void searchMenu() {
        System.out.println(SEARCH_MENU_TEXT);
        int choice = readInt("Введите номер пункта меню: ");
        switch (choice) {
            case 0 -> {
                return;
            }
            case 1 -> printList(bookingService.findByOwner(readLong("ID владельца: ")), "Ничего не найдено");
            case 2 -> printList(petService.searchByName(readString("Часть имени: ")), "Ничего не найдено");
            default -> System.out.println("Нет такого пункта");
        }
    }

    private void filterMenu() {
        System.out.println(FILTER_MENU_TEXT);
        int choice = readInt("Введите номер пункта меню: ");
        try {
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> printList(bookingService.filterByStatus(readStatus("Статус ")),
                        "Ничего не найдено");
                case 2 -> printList(bookingService.filterByDateRange(readDate("С (ГГГГ-ММ-ДД): "), readDate("По (ГГГГ-ММ-ДД): ")),
                        "Ничего не найдено");
                default -> System.out.println("Нет такого пункта");
            }
        } catch (BusinessRuleException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sortMenu() {
        System.out.println(SORT_MENU_TEXT);
        int choice = readInt("Введите номер пункта меню: ");
        switch (choice) {
            case 0 -> {
                return;
            }
            case 1 -> printList(bookingService.sortedByStartDate(), "Бронирований пока нет");
            case 2 -> printList(bookingService.sortedByCreatedAtNewestFirst(), "Бронирований пока нет");
            default -> System.out.println("Нет такого пункта");
        }
    }


    private void exportToExcel() {
        try {
            Path file = DatabaseExcelDump.export(DatabaseManager.URL, DatabaseManager.USER, DatabaseManager.PASSWORD);
            System.out.println("Файл создан: " + file);
        } catch (Exception e) {
            System.out.println("Ошибка экспорта: " + e.getMessage());
        }
    }


    private void showStatistics() {
        System.out.println("=== Статистика ===");
        for (Map.Entry<String, Long> entry : statisticsService.collect().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
