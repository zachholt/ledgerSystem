package Controller;
import Model.Account;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccountController {
    public static void crudAccount(Scanner scanner) {
        System.out.println("Would you like to:\n" + //
                "C - Create an account\n" + //
                "U - Update an account\n" + //
                "D - Delete an account\n");

        System.out.print("Selection: ");
        String selection = scanner.nextLine();
        System.out.println();


        if (selection.equalsIgnoreCase("C")) {
            createAccount(scanner);
        } else if (selection.equalsIgnoreCase("U")) {
            updateAccount(scanner);
        } else if (selection.equalsIgnoreCase("D")) {
            deleteAccount(scanner);
        } else {
            System.out.println("Invalid selection, going back to main menu.\n");
        }
    }

    public static void createAccount(Scanner scanner) {
        Account newAccount = new Account();
        System.out.print("Please Enter Customer's Name: ");
        String customerName = scanner.nextLine();

        int customerId = -1;
        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement customerQuery = connection.prepareStatement(
                    "SELECT Id FROM Customer WHERE Name = ?"
            );
            customerQuery.setString(1, customerName);
            ResultSet resultSet = customerQuery.executeQuery();

            if (resultSet.next()) {
                customerId = resultSet.getInt("Id");
                newAccount.setCustomerId(customerId);
            } else {
                System.out.println("Customer not found. Please create customer first.");
                return;
            }

            System.out.print("Please Enter Account Name: ");
            newAccount.setAccountName(scanner.nextLine());

            newAccount.setBalance(0.0);
            newAccount.setAccountNumber(generateAccountNumber());

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO Account (CustomerID, Name, AccountNumber, Balance) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setInt(1, customerId);
            preparedStatement.setString(2, newAccount.getAccountName());
            preparedStatement.setString(3, String.valueOf(newAccount.getAccountNumber()));
            preparedStatement.setDouble(4, 0.0);

            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    System.out.println("Account Created with account number: " + newAccount.getAccountNumber());
                } else {
                    System.out.println("Failed to insert data.");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static void updateAccount(Scanner scanner) {
        System.out.print("Please Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        
        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";
        
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement checkAccount = connection.prepareStatement(
                "SELECT * FROM Account WHERE AccountNumber = ?"
            );
            checkAccount.setString(1, accountNumber);
            ResultSet resultSet = checkAccount.executeQuery();
            
            if (!resultSet.next()) {
                System.out.println("Account not found.");
                return;
            }
            
            System.out.println("Please Enter New Account Name: ");
            String newAccountName = scanner.nextLine();
            
            PreparedStatement updateStmt = connection.prepareStatement(
                "UPDATE Account SET Name = ? WHERE AccountNumber = ?"
            );
            updateStmt.setString(1, newAccountName);
            updateStmt.setString(2, accountNumber);
            
            int result = updateStmt.executeUpdate();
            if (result > 0) {
                System.out.println("Account name updated successfully.");
            } else {
                System.out.println("Failed to update account name.");
            }
            
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static void deleteAccount(Scanner scanner) {
        System.out.print("Please Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        
        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";
        
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement checkAccount = connection.prepareStatement(
                "SELECT * FROM Account WHERE AccountNumber = ?"
            );
            checkAccount.setString(1, accountNumber);
            ResultSet resultSet = checkAccount.executeQuery();
            
            if (!resultSet.next()) {
                System.out.println("Account not found.");
                return;
            }
            
            PreparedStatement deleteStmt = connection.prepareStatement(
                "DELETE FROM Account WHERE AccountNumber = ?"
            );
            deleteStmt.setString(1, accountNumber);
            
            int result = deleteStmt.executeUpdate();
            if (result > 0) {
                System.out.println("Account Closed");
            } else {
                System.out.println("Failed to delete account.");
            }
            
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static String generateAccountNumber() {
        StringBuilder accountNumber = new StringBuilder();
        accountNumber.append((int) (Math.random() * 9) + 1);
        
        for (int i = 0; i < 15; i++) {
            accountNumber.append((int) (Math.random() * 10));
        }
        
        return accountNumber.toString();
    }

    public static void depositWithdraw(Scanner scanner) {
        System.out.println("Would you like to:\n" + //
                "D - Deposit Funds\n" + //
                "W - Withdraw Funds");

        System.out.print("Selection: ");
        String selection = scanner.nextLine();

        if (selection.equalsIgnoreCase("D")) {
            deposit(scanner);
        } else if (selection.equalsIgnoreCase("W")) {
            withdraw(scanner);
        } else {
            System.out.println("Invalid selection, going back to main menu.\n");
        }
    }

    public static void deposit(Scanner scanner) {
        System.out.print("Please Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        
        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";
        
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement checkAccount = connection.prepareStatement(
                "SELECT Balance FROM Account WHERE AccountNumber = ?"
            );
            checkAccount.setString(1, accountNumber);
            ResultSet resultSet = checkAccount.executeQuery();
            
            if (!resultSet.next()) {
                System.out.println("Account not found.");
                return;
            }
            
            double currentBalance = resultSet.getDouble("Balance");
            
            System.out.print("Enter deposit amount: ");
            double depositAmount = Double.parseDouble(scanner.nextLine());
            
            if (depositAmount <= 0) {
                System.out.println("Invalid deposit amount.");
                return;
            }
            
            double newBalance = currentBalance + depositAmount;
            
            connection.setAutoCommit(false);
            try {
                PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE Account SET Balance = ? WHERE AccountNumber = ?"
                );
                updateStmt.setDouble(1, newBalance);
                updateStmt.setString(2, accountNumber);
                updateStmt.executeUpdate();

                PreparedStatement transactionStmt = connection.prepareStatement(
                    "INSERT INTO \"Transaction\" (ID, Amount, Type, AccountID, MerchantName, MerchantType, TransactionDateTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                transactionStmt.setString(1, UUID.randomUUID().toString());
                transactionStmt.setDouble(2, depositAmount);
                transactionStmt.setString(3, "CREDIT");
                transactionStmt.setString(4, accountNumber);
                transactionStmt.setString(5, "Bank Deposit");
                transactionStmt.setString(6, "DEPOSIT");
                transactionStmt.setString(7, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                transactionStmt.executeUpdate();

                connection.commit();
                System.out.println("Funds Deposited\n");
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failed to process deposit.\n");
                throw e;
            }
            
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Please enter a valid number.");
        }
    }

    public static void withdraw(Scanner scanner) {
        System.out.println("Please Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        
        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";
        
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement checkAccount = connection.prepareStatement(
                "SELECT Balance FROM Account WHERE AccountNumber = ?"
            );
            checkAccount.setString(1, accountNumber);
            ResultSet resultSet = checkAccount.executeQuery();
            
            if (!resultSet.next()) {
                System.out.println("Account not found.");
                return;
            }
            
            double currentBalance = resultSet.getDouble("Balance");
            
            System.out.println("Enter withdrawal amount: ");
            double withdrawAmount = Double.parseDouble(scanner.nextLine());
            
            if (withdrawAmount <= 0) {
                System.out.println("Invalid withdrawal amount.");
                return;
            }
            
            if (withdrawAmount > currentBalance) {
                System.out.println("Insufficient funds. Current balance: $" + String.format("%.2f", currentBalance));
                return;
            }
            
            double newBalance = currentBalance - withdrawAmount;
            
            connection.setAutoCommit(false);
            try {
                PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE Account SET Balance = ? WHERE AccountNumber = ?"
                );
                updateStmt.setDouble(1, newBalance);
                updateStmt.setString(2, accountNumber);
                updateStmt.executeUpdate();

                PreparedStatement transactionStmt = connection.prepareStatement(
                    "INSERT INTO \"Transaction\" (ID, Amount, Type, AccountID, MerchantName, MerchantType, TransactionDateTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                transactionStmt.setString(1, UUID.randomUUID().toString());
                transactionStmt.setDouble(2, withdrawAmount);
                transactionStmt.setString(3, "DEBIT");
                transactionStmt.setString(4, accountNumber);
                transactionStmt.setString(5, "Bank Withdrawal");
                transactionStmt.setString(6, "WITHDRAWAL");
                transactionStmt.setString(7, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                transactionStmt.executeUpdate();

                connection.commit();
                System.out.println("Funds Withdrawn");
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failed to process withdrawal.");
                throw e;
            }
            
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Please enter a valid number.");
        }
    }
}
