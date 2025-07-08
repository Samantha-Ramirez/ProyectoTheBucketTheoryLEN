package beerbarrels;

import java.util.List;

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
    public synchronized int addBeer(int amount, List<Barrel> barrels) {
        if (currentAmount + amount <= maxCapacity) {
            // Si capacidad suficiente: agregar toda la cerveza
            currentAmount += amount;
            System.out.println("Se añadieron " + amount + "L al barril " + id + ". Litros actuales: " + currentAmount);
            notifyAll(); // Notify estudiantes en wait
            return 0;
        } else {
            // Si capacidad insuficiente: calcular overflow
            int spaceAvailable = maxCapacity - currentAmount;
            int overflow = amount - spaceAvailable;
            currentAmount = maxCapacity;
            System.out.println("Se añadieron " + spaceAvailable + "L al barril " + id + " (lleno). Litros actuales: " + currentAmount + ", desborde: " + overflow + "L");
            notifyAll(); // Notify estudiantes en wait
            
            // Transferir desborde a vecino
            if (id.equals("A")) {
                Barrel b = barrels.stream().filter(barr -> barr.id.equals("B")).findFirst().orElse(null);
                return b != null ? b.addBeer(overflow, barrels) : overflow;
            } else if (id.equals("C")) {
                Barrel b = barrels.stream().filter(barr -> barr.id.equals("B")).findFirst().orElse(null);
                return b != null ? b.addBeer(overflow, barrels) : overflow;
            } else if (id.equals("B")) {
                // Encontrar vecino (A, C) con menor currentAmount
                Barrel a = barrels.stream().filter(barr -> barr.id.equals("A")).findFirst().orElse(null);
                Barrel c = barrels.stream().filter(barr -> barr.id.equals("C")).findFirst().orElse(null);
                if (a == null || c == null) {
                    return overflow;
                }
                Barrel target = a.currentAmount <= c.currentAmount ? a : c;
                return target.addBeer(overflow, barrels);
            }
            return overflow; 
        }
    }

    // Consumir cerveza de este barril
    public synchronized int consumeBeer(int amount) {
        if (currentAmount >= amount) {
            currentAmount -= amount;
            System.out.println("Se consumieron " + amount + "L del barril " + id + ". Litros actuales: " + currentAmount);
            notifyAll(); // Notify proveedores en wait
            return amount;
        } else {
            int served = currentAmount;
            currentAmount = 0;
            System.out.println("Se consumieron " + served + "L del barril " + id + " (todo lo disponible). Litros actuales: " + currentAmount);
            notifyAll(); // Notify proveedores en wait
            return served;
        }
    }
}