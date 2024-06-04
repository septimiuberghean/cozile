package com.example.queueservice;

import java.util.LinkedList;
import java.util.Queue;

public class ServiceQueue implements Runnable {
    private Queue<Client> queue;
    private int id;
    private boolean running;

    public ServiceQueue(int id) {
        this.id = id;
        this.queue = new LinkedList<>();
        this.running = true;
    }

    public synchronized void addClient(Client client) {
        queue.add(client);
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void stopQueue() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            synchronized (this) {
                if (!queue.isEmpty()) {
                    Client client = queue.peek();
                    client.decreaseServiceTime();
                    if (client.getServiceTime() == 0) {
                        queue.poll();
                    }
                }
            }
            try {
                Thread.sleep(1000); // Simulate each second of processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized int getTotalServiceTime() {
        int totalServiceTime = 0;
        for (Client client : queue) {
            totalServiceTime += client.getServiceTime();
        }
        return totalServiceTime;
    }

    @Override
    public String toString() {
        return "Queue " + id + ": " + queue;
    }
}
