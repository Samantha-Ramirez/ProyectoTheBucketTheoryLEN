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

    public int addBeer(int amount, List<Barrel> barrels, List<String> visited, int supplier) {
        List<String> newVisited = new ArrayList<>(visited);
        newVisited.add(id);
        int spillage = 0;
        Barrel target = null;

        synchronized (this) {
            if (!id.equals("B")) {
                System.out.println("Proveedor " + supplier + " quiere añadir " + amount + "L al barril " + id + ". Litros actuales: " + currentAmount);
            }

            if (currentAmount + amount <= maxCapacity) {
                currentAmount += amount;
                if (!id.equals("B")) {
                    System.out.println("El proveedor " + supplier + " añadió " + amount + "L al barril " + id + ". Litros actuales: " + currentAmount);
                } else {
                    System.out.println("El proveedor " + supplier + " añadió por desborde " + amount + "L al barril " + id + ". Litros actuales: " + currentAmount);
                }
                notifyAll();
            } else {
                int spaceAvailable = maxCapacity - currentAmount;
                spillage = amount - spaceAvailable;
                currentAmount = maxCapacity;
                if (!id.equals("B")) {
                    System.out.println("El proveedor " + supplier + " añadió " + spaceAvailable + "L al barril " + id + " (lleno). Litros actuales: " + currentAmount + ", desborde: " + spillage + "L");
                } else {
                    System.out.println("El proveedor " + supplier + " añadió por desborde " + spaceAvailable + "L al barril " + id + " (lleno). Litros actuales: " + currentAmount + ", desborde: " + spillage + "L");
                }
                notifyAll();
            }
        }

        // Manejar overflow
        if (spillage > 0) {
            if (id.equals("A")) {
                target = barrels.stream().filter(barr -> barr.id.equals("B") && !newVisited.contains("B")).findFirst().orElse(null);
            } else if (id.equals("C")) {
                target = barrels.stream().filter(barr -> barr.id.equals("B") && !newVisited.contains("B")).findFirst().orElse(null);
            } else if (id.equals("B")) {
                Barrel a = barrels.stream().filter(barr -> barr.id.equals("A") && !newVisited.contains("A")).findFirst().orElse(null);
                Barrel c = barrels.stream().filter(barr -> barr.id.equals("C") && !newVisited.contains("C")).findFirst().orElse(null);
                if (a == null && c == null) {
                    System.out.println("Saliendo addBeer para barril " + id + ", spillage=" + spillage);
                    return spillage;
                }
                target = (a != null && (c == null || a.currentAmount <= c.currentAmount)) ? a : c;
            }
            if (target != null) {
                spillage = target.addBeer(spillage, barrels, newVisited, supplier);
            }
        }

        // Notify all barrels
        for (Barrel barrel : barrels) {
            synchronized (barrel) {
                barrel.notifyAll();
            }
        }
        return spillage;
    }

    public int addBeer(int amount, List<Barrel> barrels, int supplier) {
        return addBeer(amount, barrels, new ArrayList<>(), supplier);
    }

    public synchronized int consumeBeer(int amount) {
        if (currentAmount >= amount) {
            currentAmount -= amount;
            notifyAll();
            return amount;
        } else {
            int served = currentAmount;
            currentAmount = 0;
            notifyAll();
            return served;
        }
    }
}