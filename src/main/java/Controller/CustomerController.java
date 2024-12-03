package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

import Model.Customer;

public class CustomerController {
    public static void createCustomer(Scanner scanner) {
        Customer newCustomer = new Customer();
        
        System.out.print("Please enter customer name: ");
        newCustomer.setName(scanner.nextLine().trim());
        
        System.out.print("Please enter customer DOB (MM/DD/YYYY): ");
        newCustomer.setDateOfBirth(scanner.nextLine().trim());
        
        System.out.print("Please enter customer Phone Number: ");
        newCustomer.setPhoneNumber(scanner.nextLine().trim());
        
        System.out.print("Please enter customer Address: ");
        newCustomer.setStreetAddress(scanner.nextLine().trim());
        
        System.out.print("Please enter customer City: ");
        newCustomer.setCity(scanner.nextLine().trim());
        
        System.out.print("Please enter customer State: ");
        newCustomer.setState(scanner.nextLine().trim());
        
        System.out.print("Please enter customer Zip Code: ");
        newCustomer.setZipCode(scanner.nextLine().trim());
        
        newCustomer.setCreatedDate(String.valueOf(LocalDate.now()));

        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO Customer (Name, PhoneNumber, StreetAddress, City, State, ZipCode, CreatedDate, DOB) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, newCustomer.getName());
            preparedStatement.setString(2, newCustomer.getDateOfBirth());
            preparedStatement.setString(3, newCustomer.getPhoneNumber());
            preparedStatement.setString(4, newCustomer.getStreetAddress());
            preparedStatement.setString(5, newCustomer.getCity());
            preparedStatement.setString(6, newCustomer.getState());
            preparedStatement.setString(7, newCustomer.getZipCode());
            preparedStatement.setString(8, newCustomer.getCreatedDate());

            int resultSet = preparedStatement.executeUpdate();
            if (resultSet > 0) {
                System.out.println("Customer Created with ID: " + preparedStatement.getGeneratedKeys().getInt(1) + "\n");
            } else {
                System.out.println("\nFailed to create customer.\n");
            }
        } catch (SQLException e) {
            System.out.println("\nAn error occurred: " + e.getMessage() + "\n");
        }
    }
}
