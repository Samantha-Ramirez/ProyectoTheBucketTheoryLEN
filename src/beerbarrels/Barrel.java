package beerbarrels;

import java.util.List;
import java.util.ArrayList;

public class Barrel {
    String id;
    int maxCapacity;
    int currentAmount;

    public Barrel(String id, int maxCapacity, int currentAmount) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentAmount = currentAmount;
    }

    // Agregar cerveza a un barril, manejar desborde
    public synchronized int addBeer(int amount, List<Barrel> barrels, List<String> visited, int supplier) {
        // Add current barrel to visited list
        List<String> newVisited = new ArrayList<>(visited);
        newVisited.add(id);

        if (currentAmount + amount <= maxCapacity) {
            // Si capacidad suficiente: agregar toda la cerveza
            currentAmount += amount;
            System.out.println("El proveedor "+ supplier +" añadio " + amount + "L al barril " + id + ". Litros actuales: " + currentAmount);
            notifyAll();
            return 0;
        } else {
            // Si capacidad insuficiente: calcular overflow
            int spaceAvailable = maxCapacity - currentAmount;
            int overflow = amount - spaceAvailable;
            currentAmount = maxCapacity;
            System.out.println("El proveedor "+ supplier +" añadio " + spaceAvailable + "L al barril " + id + " (lleno). Litros actuales: " + currentAmount + ", desborde: " + overflow + "L");
            notifyAll();
            
            // Transferir desborde a vecino, evitando barriles ya visitados
            if (id.equals("A")) {
                Barrel b = barrels.stream().filter(barr -> barr.id.equals("B") && !newVisited.contains("B")).findFirst().orElse(null);
                return b != null ? b.addBeer(overflow, barrels, newVisited) : overflow;
            } else if (id.equals("C")) {
                Barrel b = barrels.stream().filter(barr -> barr.id.equals("B") && !newVisited.contains("B")).findFirst().orElse(null);
                return b != null ? b.addBeer(overflow, barrels, newVisited) : overflow;
            } else if (id.equals("B")) {
                Barrel a = barrels.stream().filter(barr -> barr.id.equals("A") && !newVisited.contains("A")).findFirst().orElse(null);
                Barrel c = barrels.stream().filter(barr -> barr.id.equals("C") && !newVisited.contains("C")).findFirst().orElse(null);
                if (a == null && c == null) {
                    return overflow;
                }
                Barrel target = (a != null && (c == null || a.currentAmount <= c.currentAmount)) ? a : c;
                return target != null ? target.addBeer(overflow, barrels, newVisited) : overflow;
            }
            return overflow;
        }
    }

    // Wrapper method for initial call
    public synchronized int addBeer(int amount, List<Barrel> barrels) {
        return addBeer(amount, barrels, new ArrayList<>());
    }

    // Consumir cerveza de este barril
    public synchronized int consumeBeer(int amount) {
        if (currentAmount >= amount) {
            currentAmount -= amount;
            // Print moved to StudentThread.java
            notifyAll();
            return amount;
        } else {
            int served = currentAmount;
            currentAmount = 0;
            // Print moved to StudentThread.java
            notifyAll();
            return served;
        }
    }
}
