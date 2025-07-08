package beerbarrels;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barrel {
    String id;
    int maxCapacity;
    int currentAmount;
    Lock lock;

    public Barrel(String id, int maxCapacity, int currentAmount) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentAmount = currentAmount;
        this.lock = new ReentrantLock();
    }
}