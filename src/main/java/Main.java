import java.util.Scanner;

import Controller.AccountController;
import Controller.CustomerController;
import Controller.TransactionController;
import Controller.ReportsController;
import Model.DatabaseCreator;

public class Main {
    public static void main(String[] args) {
        DatabaseCreator.createDatabase();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Code School Bank of Awesomeness!\n-----------------------------------------------\n");

        while (true) {
            printMenu();
            System.out.print("Selection: ");
            String selection = scanner.nextLine();
            System.out.println();

            if (selection.equalsIgnoreCase("x")) {
                break;
            } else if (selection.equalsIgnoreCase("1")) {
                ReportsController.generateStatement(scanner);
            } else if (selection.equalsIgnoreCase("2")) {
                ReportsController.generateReport(scanner);
            } else if (selection.equalsIgnoreCase("3")) {
                TransactionController.simulateTransaction(scanner);
            } else if (selection.equalsIgnoreCase("4")) {
                CustomerController.createCustomer(scanner);
            } else if (selection.equalsIgnoreCase("5")) {
                AccountController.crudAccount(scanner);
            } else if (selection.equalsIgnoreCase("6")) {
                AccountController.depositWithdraw(scanner);
            }
        }
        scanner.close();
    }

    public static void printMenu() {
        System.out.println("Select from the following:\n" +
                "1. Generate a Statement\n" +
                "2. Generate Accounting Reports\n" +
                "3. Simulate a Transaction for Account\n" +
                "4. Create a Customer\n" +
                "5. CRUD a Customer Account\n" +
                "6. Deposit/Withdrawal Funds\n" +
                "X. Exit\n");
    }
}
