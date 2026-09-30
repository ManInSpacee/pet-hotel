package ru.mirea.project;

import ru.mirea.project.repository.*;
import ru.mirea.project.service.*;
import ru.mirea.project.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {

        OwnerRepository ownerRepository = new JdbcOwnerRepository();
        EnclosureRepository enclosureRepository = new JdbcEnclosureRepository();
        BookingRepository bookingRepository = new JdbcBookingRepository();
        PetRepository petRepository = new JdbcPetRepository();

        OwnerService ownerService = new OwnerService(ownerRepository, bookingRepository);
        EnclosureService enclosureService = new EnclosureService(bookingRepository, enclosureRepository);
        BookingService bookingService = new BookingService(bookingRepository, enclosureRepository, petRepository);
        PetService petService = new PetService(petRepository, ownerRepository, bookingRepository);
        StatisticsService statisticsService =
                new StatisticsService(ownerRepository, petRepository, enclosureRepository, bookingRepository);

        ConsoleMenu menu = new ConsoleMenu(bookingService, ownerService, petService, enclosureService, statisticsService);
        menu.run();
    }
}
