package Model;

public class Account {
    private int id;
    private int customerId;
    private Double balance;
    private String accountName;
    private String accountNumber;

    public int getId() {
        return id;
    }

    public Account setId(int id) {
        this.id = id;
        return this;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Account setCustomerId(int customerId) {
        this.customerId = customerId;
        return this;
    }

    public Double getBalance() {
        return balance;
    }

    public Account setBalance(Double balance) {
        this.balance = balance;
        return this;
    }

    public String getAccountName() {
        return accountName;
    }

    public Account setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Account setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }
}

