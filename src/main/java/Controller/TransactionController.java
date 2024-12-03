package Controller;

import Model.Transaction;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;

import com.google.gson.Gson;

public class TransactionController {
    public static void simulateTransaction(Scanner scanner) {
        System.out.print("Please enter the account number: ");
        String accountNumber = scanner.nextLine();

        System.out.println("Fetching Transaction Data...");
        try {
            performTransaction(accountNumber);
            System.out.println("Transaction Simulated\n");
        } catch (Exception e) {
            System.out.println("Error simulating transaction: " + e.getMessage() + "\n");
        }
    }

    private static void performTransaction(String accountNumber) throws IOException, InterruptedException {
        String url = "jdbc:sqlite:/Users/zachholt/Desktop/ledgerSystem.db";
        String apiUrl = "http://3.145.72.203:8080/transaction/" + accountNumber;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Gson gson = new Gson();
        Transaction transaction = gson.fromJson(responseBody, Transaction.class);

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO \"Transaction\" (ID, Amount, Type, AccountID, MerchantName, MerchantType, TransactionDateTime) VALUES (?, ?, ?, ?, ?, ?, ?)");

            pstmt.setString(1, transaction.getTransactionId().toString());
            pstmt.setString(2, transaction.getTransactionType().toString());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, accountNumber);
            pstmt.setString(5, transaction.getRecipient().getMerchantName());
            pstmt.setString(6, transaction.getRecipient().getMerchantType());
            pstmt.setString(7, LocalDateTime.now().toString());
            int result = pstmt.executeUpdate();
            if (result > 0) {
                // System.out.println("Transaction logged successfully");
            } else {
                // System.out.println("Failed to log transaction");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
