

package com.MunivelBank.AtmMachineproject;
import java.util.concurrent.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Atm {
    public static void main(String[] args) {
        try {
            // Initialize Scanner for user input
            Scanner sc = new Scanner(System.in);
            System.out.println("Hey welcome to all-in-one ATM");
            System.out.println("Press 1 to Create an Account");
            System.out.println("Press 2 to Process transaction");
            int create = sc.nextInt();

            // Create an account
            if (create == 1) {
                createAccount(sc);
            } 
            // Process transaction
            else if (create == 2) {
                processTransaction(sc);
            } 
            else {
                System.out.println("Invalid option.");
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    private static void createAccount(Scanner sc) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Atm", "root", "Munivel@9787");
            int length = 4;

            System.out.println("Welcome to Our Bank");
            System.out.print("Enter Account Holder Name: ");
            String name = sc.next();

            System.out.print("Enter the Account Number: ");
            long accountNumber = sc.nextLong();

            System.out.print("Enter the Four Digit Pin Password: ");
            String pin = sc.next();
            System.out.print("Enter the Confirm Pin: ");
            String confirmPin = sc.next();

            System.out.print("Deposit minimum amount 500 for your newly created Account: ");
            int balance = sc.nextInt();

            if (pin.equals(confirmPin) && pin.length() == length) {
                String query = "INSERT INTO Atmuser (name, account_num, pins, balance) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, name);
                ps.setLong(2, accountNumber);
                ps.setString(3, pin);
                ps.setInt(4, balance);
                int row = ps.executeUpdate();
                if (row > 0) {
                    System.out.println("Account Created Successfully...");
                }
            } else {
                System.out.println("Make sure the entered Pin is valid...");
            }
        } catch (Exception e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    private static void processTransaction(Scanner sc) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Atm", "root", "Munivel@9787");
            Statement st = con.createStatement();

            System.out.print("Enter your pin number: ");
            String pin = sc.next();

            ResultSet rs = st.executeQuery("SELECT * FROM Atmuser WHERE pins='" + pin + "'");
            if (!rs.next()) {
                System.out.println("Wrong pin number");
                return;
            }

            String name = rs.getString("name");
            int balance = rs.getInt("balance");
            System.out.println("Hello " + name);

            while (true) {
                System.out.println("Press 1 to Check Balance");
                System.out.println("Press 2 to Add amount");
                System.out.println("Press 3 to Withdrawal Amount");
                System.out.println("Press 4 to Print the Receipt");
                System.out.println("Press 5 to Exit");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        System.out.println("Your balance is: " + balance);
                        break;
                    case 2:
                        System.out.print("Enter the amount to Add: ");
                        int amountToAdd = sc.nextInt();
                        balance += amountToAdd;
                        st.executeUpdate("UPDATE Atmuser SET balance=" + balance + " WHERE pins='" + pin + "'");
                        System.out.println("Successfully added. Current amount: " + balance);
                        break;
                    case 3:
                        NetworkSpeedChecker networkChecker = new NetworkSpeedChecker();
                        String result = networkChecker.withdrawalCheck();
                        if (result.equals("Network conditions are acceptable. You can proceed with the transaction.")) {
                            System.out.print("Enter the amount to withdraw: ");
                            int takeAmount = sc.nextInt();
                            
                            ///
                            if (takeAmount > balance) {
                                System.out.println("Your balance is insufficient.");
                            } else {
                            
                                st.executeUpdate("UPDATE Atmuser SET balance=" + balance + " WHERE pins='" + pin + "'");
                                System.out.println("Successfully withdrawn. Current balance: " + balance);
                            }
                        } else {
                            System.out.println(result);
                        }
                        break;
                    case 4:
                        System.out.println("Thank you for coming. Your current balance is: " + balance);
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing transaction: " + e.getMessage());
        }
    }
}





 class WithdrawalTransaction {
    private static final int MAX_RETRIES = 5;
    private static final double MIN_NETWORK_SPEED = 100.0; // Minimum acceptable network speed (Mbps)
    private static final double MAX_PACKET_LOSS = 0.1; // Maximum acceptable packet loss (10%)
    private static final long MAX_LATENCY = 200; // Maximum acceptable latency (ms)
    private static double accountBalance = 500.0; // Initial account balance
    private static final Object lock = new Object(); // Lock for synchronization

    public void  amount(double amount ) {
        double amountToWithdraw = amount; // Example amount

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> processWithdrawal(amountToWithdraw));

        try {
            if (future.get()) {
                System.out.println("Transaction successful!");
            }
        } catch (Exception e) {
            System.out.println("Sorry, while the transaction began, we found some issues: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static boolean processWithdrawal(double amount) throws Exception {
        if (!isNetworkConditionGood()) {
            throw new Exception("Network conditions are not suitable for withdrawal.");
        }

        boolean transactionCompleted = false;
        int attempt = 0;

        while (attempt < MAX_RETRIES && !transactionCompleted) {
            try {
                transactionCompleted = initiateTransaction(amount);
                if (!transactionCompleted) {
                    throw new Exception("Transaction failed, retrying...");
                }
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new Exception("Transaction failed after multiple attempts.");
                }
            }
        }
        return transactionCompleted;
    }

    private static boolean initiateTransaction(double amount) {
        synchronized (lock) { // Synchronize to ensure thread safety
            if (amount <= accountBalance) {
                System.out.println("Initiating transaction for amount: " + amount);
                // Simulate transaction processing time
                try {
                    Thread.sleep(100); // Simulate processing delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Simulate random success or failure
                boolean success = Math.random() > 0.2; // 80% success rate
                if (success) {
                    accountBalance -= amount; // Debit the amount
                    System.out.println("Transaction successful, new balance: " + accountBalance);
                } else {
                    System.out.println("Transaction failed.");
                }
                return success;
            } else {
                System.out.println("Insufficient balance for the transaction.");
                return false;
            }
        }
    }

    private static boolean isNetworkConditionGood() {
        double networkSpeed = getNetworkSpeed();
        double packetLoss = getPacketLoss();
        long latency = getLatency();

        return networkSpeed >= MIN_NETWORK_SPEED && packetLoss <= MAX_PACKET_LOSS && latency <= MAX_LATENCY;
    }

    private static double getNetworkSpeed() {
        return 100.0 + Math.random() * 50; // Mock value between 100 and 150 Mbps
    }

    private static double getPacketLoss() {
        return Math.random() * 0.2; // Mock value between 0 and 0.2 (20%)
    }

    private static long getLatency() {
        return (long) (Math.random() * 300); // Mock value between 0 and 300 ms
    }
}


