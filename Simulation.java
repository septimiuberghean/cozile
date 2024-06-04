package com.example.queueservice;

import java.util.ArrayList;
import java.util.List;
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

    public Simulation(int numClients, int numQueues, int simulationMaxTime, int minArrivalTime,
                      int maxArrivalTime, int minServiceTime, int maxServiceTime) {
        this.numClients = numClients;
        this.numQueues = numQueues;
        this.simulationMaxTime = simulationMaxTime;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minServiceTime = minServiceTime;
        this.maxServiceTime = maxServiceTime;
        this.clients = new ArrayList<>();
        this.serviceQueues = new ArrayList<>();

        generateClients();
        initializeQueues();
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
            // Find the client with the earliest arrival time
            Client nextClient = null;
            for (Client client : clients) {
                if (client.getArrivalTime() <= currentTime) {
                    if (nextClient == null || client.getArrivalTime() < nextClient.getArrivalTime()) {
                        nextClient = client;
                    }
                }
            }

            // If a client is found, add it to the queue with the shortest total service time
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

            logStatus(currentTime);

            try {
                Thread.sleep(1000); // Simulate each second
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

        logFinalStatus();
    }

    private void logStatus(int currentTime) {
        System.out.println("Time " + currentTime);
        System.out.println("Waiting clients: " + clients);
        for (ServiceQueue queue : serviceQueues) {
            System.out.println(queue);
        }
        System.out.println();
    }

    private void logFinalStatus() {
        System.out.println("Simulation finished.");
    }
}
