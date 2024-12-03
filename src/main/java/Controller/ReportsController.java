package Controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import Model.Account;
import Model.Customer;
import Model.Transaction;

public class ReportsController {
    private static final String DATABASE_URL = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";


    public static void generateReport(Scanner scanner) {
        String outputPath = getOutputPath(scanner);
        if (outputPath == null) return;

        generateCustomerAccountReport(outputPath);
        System.out.println();
        System.out.println("Generated Accounting Report\n");
    }

    public static void generateStatement(Scanner scanner) {
        String outputPath = getOutputPath(scanner);
        if (outputPath == null) return;
        
        System.out.print("Please enter customer name: ");
        String customerName = scanner.nextLine().trim();

        generateCustomerReport(customerName, outputPath);
        System.out.println();
        System.out.println("Generated Statement for " + customerName + "\n");
    }

    private static String getOutputPath(Scanner scanner) {
        String outputPath = "";
        boolean validPath = false;
        
        while (!validPath) {
            System.out.print("Please Enter a Valid File Path to Create the File: ");
            outputPath = scanner.nextLine().trim();
            
            File directory = new File(outputPath);
            
            if (!directory.exists() || !directory.isDirectory() || !directory.canWrite()) {
                System.out.println("Invalid directory, Try again.");
            } else {
                validPath = true;
            }
        }
        
        return outputPath;
    }

    public static void generateCustomerAccountReport(String outputPath) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String summaryQuery = "SELECT " +
                    "(SELECT COUNT(*) FROM Customer) as customerCount, " +
                    "(SELECT COUNT(*) FROM Account) as accountCount, " +
                    "(SELECT SUM(Balance) FROM Account) as totalBalance";

            int totalCustomers = 0;
            int totalAccounts = 0;
            double totalBalance = 0.0;

            try (PreparedStatement pstmt = connection.prepareStatement(summaryQuery)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalCustomers = rs.getInt("customerCount");
                    totalAccounts = rs.getInt("accountCount");
                    totalBalance = rs.getDouble("totalBalance");
                }
            }

            String accountInfoQuery = "SELECT c.ID as CustomerID, c.Name as CustomerName, " +
                    "a.ID as AccountID, a.Name as AccountName, a.Balance " +
                    "FROM Customer c " +
                    "LEFT JOIN Account a ON c.ID = a.CustomerID " +
                    "ORDER BY c.Name";

            Map<String, Customer> customers = new HashMap<>();
            Map<String, List<Account>> customerAccounts = new HashMap<>();

            try (PreparedStatement pstmt = connection.prepareStatement(accountInfoQuery)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String customerId = rs.getString("CustomerID");
                    String customerName = rs.getString("CustomerName");

                    if (!customers.containsKey(customerId)) {
                        Customer customer = new Customer();
                        customer.setId(customerId);
                        customer.setName(customerName);
                        customers.put(customerId, customer);
                        customerAccounts.put(customerId, new ArrayList<>());
                    }

                    if (rs.getString("AccountID") != null) {
                        Account account = new Account();
                        account.setId(rs.getInt("AccountID"));
                        account.setAccountName(rs.getString("AccountName"));
                        account.setBalance(rs.getDouble("Balance"));
                        customerAccounts.get(customerId).add(account);
                    }
                }
            }

            StringBuilder report = new StringBuilder();
            report.append("Bank Accounting Report\n\n");

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            report.append("Report Date: ").append(today.format(formatter)).append("\n\n");

            report.append(String.format("Total Customers: %d\n", totalCustomers));
            report.append(String.format("Total Accounts: %d\n", totalAccounts));
            report.append(String.format("Total Balance: $%,.2f\n\n", totalBalance));

            for (Map.Entry<String, Customer> entry : customers.entrySet()) {
                Customer customer = entry.getValue();
                report.append(customer.getName()).append(":\n");

                for (Account account : customerAccounts.get(entry.getKey())) {
                    report.append(String.format("%s - $%,.2f\n",
                            account.getAccountName(), account.getBalance()));
                }
                report.append("\n");
            }

            String reportPath = outputPath + "/" + "customer_account_report.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportPath))) {
                writer.write(report.toString());
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

    public static void generateCustomerReport(String customerName, String outputPath) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String customerQuery = "SELECT c.Name as CustomerName, " +
                    "a.ID as AccountID, a.Name as AccountName, a.Balance, " +
                    "t.Amount as TransactionAmount, t.Type as TransactionType, " +
                    "t.TransactionDateTime, t.MerchantName " +
                    "FROM Customer c " +
                    "LEFT JOIN Account a ON c.ID = a.CustomerID " +
                    "LEFT JOIN \"Transaction\" t ON a.ID = t.AccountID " +
                    "WHERE c.Name = ? " +
                    "ORDER BY a.Name, t.TransactionDateTime DESC";

            Map<String, Account> accounts = new HashMap<>();
            Map<String, List<Transaction>> accountTransactions = new HashMap<>();
            double totalBalance = 0.0;
            boolean customerFound = false;

            try (PreparedStatement pstmt = connection.prepareStatement(customerQuery)) {
                pstmt.setString(1, customerName);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    customerFound = true;
                    String accountId = rs.getString("AccountID");

                    if (accountId != null && !accounts.containsKey(accountId)) {
                        Account account = new Account();
                        account.setId(Integer.parseInt(accountId));
                        account.setAccountName(rs.getString("AccountName"));
                        account.setBalance(rs.getDouble("Balance"));
                        accounts.put(accountId, account);
                        accountTransactions.put(accountId, new ArrayList<>());
                        totalBalance += account.getBalance();
                    }

                    if (rs.getString("TransactionDateTime") != null) {
                        Transaction transaction = new Transaction();
                        transaction.setAmount(rs.getDouble("TransactionAmount"));
                        transaction.setTransactionType(Transaction.TransactionType.valueOf(rs.getString("TransactionType")));
                        Transaction.Recipient recipient = new Transaction.Recipient();
                        recipient.setMerchantName(rs.getString("MerchantName"));
                        transaction.setRecipient(recipient);
                        accountTransactions.get(accountId).add(transaction);
                    }
                }
            }

            if (!customerFound) {
                System.out.println("Customer not found: " + customerName);
                return;
            }

            StringBuilder report = new StringBuilder();
            report.append("Bank Accounting Report\n\n");

            report.append("Statement for ").append(customerName).append("\n");

            LocalDate today = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            report.append("Statement Date: ").append(today.format(dateFormatter)).append("\n\n");

            report.append(String.format("Current Total Balance: $%,.2f\n\n", totalBalance));

            DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            for (Map.Entry<String, Account> entry : accounts.entrySet()) {
                Account account = entry.getValue();
                report.append(String.format("%s - $%,.2f\n", account.getAccountName(), account.getBalance()));
                report.append("Transactions:\n");

                for (Transaction transaction : accountTransactions.get(entry.getKey())) {
                    String prefix = transaction.getTransactionType() == Transaction.TransactionType.CREDIT ? "+" : "-";
                    report.append(String.format("%s %s $%.2f\n",
                            LocalDateTime.now().format(transactionFormatter),
                            transaction.getRecipient().getMerchantName(),
                            Math.abs(transaction.getAmount())
                    ).replace("$", prefix + "$"));
                }
                report.append("\n");
            }

            String reportPath = outputPath + "/" + customerName + "_statement.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportPath))) {
                writer.write(report.toString());
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error generating customer report: " + e.getMessage());
        }
    }
} 