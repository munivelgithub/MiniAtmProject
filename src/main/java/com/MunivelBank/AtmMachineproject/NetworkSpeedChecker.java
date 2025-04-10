//package com.MunivelBank.AtmMachineproject;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class NetworkSpeedChecker {
//    
//    private static final String TEST_URL = "https://canarabank.com"; 
//    private static final long MIN_SPEED_MBPS = 1; 
//    private static final long MAX_LATENCY_MS = 10_000; 
//    private static final double MAX_PACKET_LOSS_PERCENTAGE = 1; 
//    private static final long TRANSACTION_TIMEOUT_MS = 30_000; // Timeout for transactions
//
//    private final AtomicBoolean isTransactionInProgress = new AtomicBoolean(false);
//    public String withdrawalCheck() {
//        if (isTransactionInProgress.get()) {
//            return "Transaction already in progress.";
//        }
//
//        isTransactionInProgress.set(true);
//        
//        try {
//            long speed = measureDownloadSpeed(TEST_URL);
//            long latency = measureLatency(TEST_URL);
//            double packetLoss = measurePacketLoss();
//            updateHistoricalData(speed, latency, packetLoss);
//
//            return alertUser(speed, latency, packetLoss);
//        } catch (IOException e) {
//            return "Error occurred while checking network conditions: " + e.getMessage();
//        } finally {
//            isTransactionInProgress.set(false);
//        }
//    }
//
//    private long measureDownloadSpeed(String urlString) throws IOException {
//        HttpGet request = new HttpGet(urlString);
//        long totalBytes = 0;
//        long startTime = System.currentTimeMillis();
//
//        try (CloseableHttpClient httpClient = HttpClients.createDefault();
//             CloseableHttpResponse response = httpClient.execute(request)) {
//             
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                try (InputStream in = new BufferedInputStream(entity.getContent())) {
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = in.read(buffer)) != -1) {
//                        totalBytes += bytesRead;
//                    }
//                }
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        long durationInSeconds = (endTime - startTime) / 1000;
//        return (durationInSeconds > 0) ? (totalBytes * 8) / (durationInSeconds * 1024 * 1024) : 0;
//    }
//
//    private long measureLatency(String urlString) throws IOException {
//        long startTime = System.currentTimeMillis();
//        try (CloseableHttpClient httpClient = HttpClients.createDefault();
//             CloseableHttpResponse response = httpClient.execute(new HttpGet(urlString))) {
//        }
//        long endTime = System.currentTimeMillis();
//        return endTime - startTime;
//    }
//
//    private double measurePacketLoss() {
//        return Math.random() * 10; // Simulating random packet loss
//    }
//
//    private String alertUser(long speed, long latency, double packetLoss) {
//        if (speed < MIN_SPEED_MBPS || (latency > MAX_LATENCY_MS) || (packetLoss > MAX_PACKET_LOSS_PERCENTAGE)) {
//            return "Alert: Network speed is too low. Avoid taking funds.";   
//        }
//        return "Network conditions are acceptable. You can proceed with the transaction.";
//    }
//
//    private void updateHistoricalData(long speed, long latency, double packetLoss) {
//    }
//
//    public void monitorTransaction() {
//        new Thread(() -> {
//            long startTime = System.currentTimeMillis();
//            while (isTransactionInProgress.get() && (System.currentTimeMillis() - startTime) < TRANSACTION_TIMEOUT_MS) {
//                try {
//                    long speed = measureDownloadSpeed(TEST_URL);
//                    long latency = measureLatency(TEST_URL);
//                    double packetLoss = measurePacketLoss();
//                    
//                    updateHistoricalData(speed, latency, packetLoss);
//
//                    if (speed < MIN_SPEED_MBPS || (latency > MAX_LATENCY_MS) || (packetLoss > MAX_PACKET_LOSS_PERCENTAGE)) {
//                        // Logic to cancel or rollback the transaction
//                        System.out.println("Transaction aborted due to network issues.");
//                        // Additional logic for handling rollback can be added here
//                        break;
//                    }
//
//                    Thread.sleep(2000); // Check every 2 seconds
//                } catch (IOException | InterruptedException e) {
//                    System.out.println("Error during monitoring: " + e.getMessage());
//                }
//            }
//        }).start();
//    }
//}


package com.MunivelBank.AtmMachineproject;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkSpeedChecker {

    private static final String TEST_URL = "https://canarabank.com"; 
    private static final long MIN_SPEED_MBPS = 1; 
    private static final long MAX_LATENCY_MS = 10_000; 
    private static final double MAX_PACKET_LOSS_PERCENTAGE = 1; 
    private static final long TRANSACTION_TIMEOUT_MS = 30_000; // Timeout for transactions

    private final AtomicBoolean isTransactionInProgress = new AtomicBoolean(false);
    
    // Variables to store historical data
    private final Queue<Long> speedHistory = new LinkedList<>();
    private final Queue<Long> latencyHistory = new LinkedList<>();
    private final Queue<Double> packetLossHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 5; // Size of the historical data window

    public String withdrawalCheck() {
        if (isTransactionInProgress.get()) {
            return "Transaction already in progress.";
        }

        isTransactionInProgress.set(true);
        
        try {
            long speed = measureDownloadSpeed(TEST_URL);
            long latency = measureLatency(TEST_URL);
            double packetLoss = measurePacketLoss();
            updateHistoricalData(speed, latency, packetLoss);

            return alertUser();
        } catch (IOException e) {
            return "Error occurred while checking network conditions: " + e.getMessage();
        } finally {
            isTransactionInProgress.set(false);
        }
    }

    private long measureDownloadSpeed(String urlString) throws IOException {
        HttpGet request = new HttpGet(urlString);
        long totalBytes = 0;
        long startTime = System.currentTimeMillis();

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
             
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream in = new BufferedInputStream(entity.getContent())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long durationInSeconds = (endTime - startTime) / 1000;
        return (durationInSeconds > 0) ? (totalBytes * 8) / (durationInSeconds * 1024 * 1024) : 0;
    }

    private long measureLatency(String urlString) throws IOException {
        long startTime = System.currentTimeMillis();
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(urlString))) {
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private double measurePacketLoss() {
        return Math.random() * 10; // Simulating random packet loss
    }

    private String alertUser() {
        long avgSpeed = (long) speedHistory.stream().mapToLong(Long::longValue).average().orElse(0);
        long avgLatency = (long) latencyHistory.stream().mapToLong(Long::longValue).average().orElse(Long.MAX_VALUE);
        double avgPacketLoss = packetLossHistory.stream().mapToDouble(Double::doubleValue).average().orElse(Double.MAX_VALUE);
        
        if (avgSpeed < MIN_SPEED_MBPS || (avgLatency > MAX_LATENCY_MS) || (avgPacketLoss > MAX_PACKET_LOSS_PERCENTAGE)) {
            return "Alert: Network speed is too low. Avoid taking funds.";   
        }
        return "Network conditions are acceptable. You can proceed with the transaction.";
    }

    private void updateHistoricalData(long speed, long latency, double packetLoss) {
        if (speedHistory.size() >= HISTORY_SIZE) speedHistory.poll();
        speedHistory.offer(speed);

        if (latencyHistory.size() >= HISTORY_SIZE) latencyHistory.poll();
        latencyHistory.offer(latency);

        if (packetLossHistory.size() >= HISTORY_SIZE) packetLossHistory.poll();
        packetLossHistory.offer(packetLoss);
    }

    public void monitorTransaction() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (isTransactionInProgress.get() && (System.currentTimeMillis() - startTime) < TRANSACTION_TIMEOUT_MS) {
                try {
                    long speed = measureDownloadSpeed(TEST_URL);
                    long latency = measureLatency(TEST_URL);
                    double packetLoss = measurePacketLoss();
                    
                    updateHistoricalData(speed, latency, packetLoss);

                    if (alertUser().startsWith("Alert:")) {
                        // Logic to cancel or rollback the transaction
                        System.out.println("Transaction aborted due to network issues.");
                        break;
                    }

                    Thread.sleep(2000); // Check every 2 seconds
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error during monitoring: " + e.getMessage());
                }
            }
        }).start();
    }
}
