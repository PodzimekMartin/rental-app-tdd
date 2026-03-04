package cz.podzimek.rental;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        RentalService service = new RentalService();

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println();
            System.out.println("Rental System");
            System.out.println("1 - Add item (ADMIN)");
            System.out.println("2 - Rent item");
            System.out.println("3 - Return item");
            System.out.println("0 - Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                break;
            }

            try {

                switch (choice) {

                    case 1 -> {
                        System.out.print("Item ID: ");
                        String itemId = scanner.nextLine();

                        service.addItem(UserRole.ADMIN, itemId);

                        System.out.println("Item added");
                    }

                    case 2 -> {
                        System.out.print("Item ID: ");
                        String itemId = scanner.nextLine();

                        service.rentItem(itemId);

                        System.out.println("Item rented");
                    }

                    case 3 -> {
                        System.out.print("Item ID: ");
                        String itemId = scanner.nextLine();

                        service.returnItem(itemId);

                        System.out.println("Item returned");
                    }

                    default -> System.out.println("Unknown option");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        }

        System.out.println("Application finished");
    }
}