package com.example.queueservice;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Simulation implements Runnable {
    private int numClients;
    private int numQueues;
    private int simulationMaxTime;
    private int minArrivalTime;
    private int maxArrivalTime;
    private int minServiceTime;
    private int maxServiceTime;
    private List<Client> clients;
    private List<ServiceQueue> serviceQueues;
    private BufferedWriter writer;
    private TextArea logTextArea;
    private Queue<String> messages;
    private Timeline timeline;

    public Simulation(int numClients, int numQueues, int simulationMaxTime, int minArrivalTime,
                      int maxArrivalTime, int minServiceTime, int maxServiceTime, TextArea logTextArea) {
        this.numClients = numClients;
        this.numQueues = numQueues;
        this.simulationMaxTime = simulationMaxTime;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minServiceTime = minServiceTime;
        this.maxServiceTime = maxServiceTime;
        this.clients = new ArrayList<>();
        this.serviceQueues = new ArrayList<>();
        this.logTextArea = logTextArea;
        try {
            this.writer = new BufferedWriter(new FileWriter("simulation_log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateClients();
        initializeQueues();
        this.messages = new LinkedList<>();
        this.timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateLog()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }
    private void generateClients() {
        Random random = new Random();
        for (int i = 1; i <= numClients; i++) {
            int arrivalTime = minArrivalTime + random.nextInt(maxArrivalTime - minArrivalTime + 1);
            int serviceTime = minServiceTime + random.nextInt(maxServiceTime - minServiceTime + 1);
            clients.add(new Client(i, arrivalTime, serviceTime));
        }
    }
    private void initializeQueues() {
        for (int i = 1; i <= numQueues; i++) {
            serviceQueues.add(new ServiceQueue(i));
        }
    }
    @Override
    public void run() {
        int currentTime = 0;
        List<Thread> queueThreads = new ArrayList<>();
        for (ServiceQueue queue : serviceQueues) {
            Thread thread = new Thread(queue);
            queueThreads.add(thread);
            thread.start();
        }
        while (currentTime <= simulationMaxTime || !clients.isEmpty()) {
            Client nextClient = null;
            for (Client client : clients) {
                if (client.getArrivalTime() <= currentTime) {
                    if (nextClient == null || client.getArrivalTime() < nextClient.getArrivalTime()) {
                        nextClient = client;
                    }
                }
            }

            if (nextClient != null) {
                ServiceQueue bestQueue = serviceQueues.get(0);
                for (ServiceQueue queue : serviceQueues) {
                    if (queue.getTotalServiceTime() < bestQueue.getTotalServiceTime()) {
                        bestQueue = queue;
                    }
                }
                bestQueue.addClient(nextClient);
                clients.remove(nextClient);
            }

            updateMessages(currentTime);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            currentTime++;
        }

        for (ServiceQueue queue : serviceQueues) {
            queue.stopQueue();
        }

        for (Thread thread : queueThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            writer.write("Simulation finished.");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMessages(int currentTime) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Time ").append(currentTime).append("\n");
        messageBuilder.append("Waiting clients: ").append(clients).append("\n");
        for (ServiceQueue queue : serviceQueues) {
            messageBuilder.append(queue.toString()).append("\n");
        }
        String message = messageBuilder.toString();
        messages.add(message);

        try {
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!timeline.getStatus().equals(Timeline.Status.RUNNING)) {
            timeline.play();
        }
    }


    private void updateLog() {
        if (!messages.isEmpty()) {
            logTextArea.setText(messages.poll());
        } else {
            timeline.stop();
        }
    }
}