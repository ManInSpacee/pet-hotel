package ru.mirea.project;

import ru.mirea.project.exception.BusinessRuleException;
import ru.mirea.project.model.*;
import ru.mirea.project.repository.*;
import ru.mirea.project.service.BookingService;
import ru.mirea.project.service.EnclosureService;
import ru.mirea.project.service.OwnerService;
import ru.mirea.project.service.PetService;
import ru.mirea.project.ui.ConsoleMenu;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        System.out.println("pet-hotel");

        // тестирование сервисов EnclosureService и OwnerService
        OwnerRepository ownerRepository = new JdbcOwnerRepository();
        EnclosureRepository enclosureRepository = new JdbcEnclosureRepository();
        BookingRepository bookingRepository = new JdbcBookingRepository();
        PetRepository petRepository = new JdbcPetRepository();

        OwnerService ownerService = new OwnerService(ownerRepository, bookingRepository);
        EnclosureService enclosureService = new EnclosureService(bookingRepository, enclosureRepository);
        BookingService bookingService = new BookingService(bookingRepository, enclosureRepository, petRepository);
        PetService petService = new PetService(petRepository, ownerRepository, bookingRepository);

        ConsoleMenu menu = new ConsoleMenu(bookingService, ownerService, petService, enclosureService);
       menu.run();

    }
}
