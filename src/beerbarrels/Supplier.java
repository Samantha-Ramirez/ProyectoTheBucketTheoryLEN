package beerbarrels;

import java.util.List;

public class Supplier implements Runnable {
    private List<Barrel> barrels;
    private String targetBarrel;
    private static final int BEER_TO_ADD = 1;

    public Supplier(List<Barrel> barrels, String targetBarrel) {
        this.barrels = barrels;
        this.targetBarrel = targetBarrel;
    }

    @Override
    public void run() {
        // Validar barril objetivo (solo A o C permitido)
        if (!targetBarrel.equals("A") && !targetBarrel.equals("C")) {
            System.out.println("Error: proveedor intentó añadir cerveza al barril " + targetBarrel + ", solo A o C permitidos.");
            BeerBarrels.addSpillage(0);
            return;
        }

        Barrel barrel = barrels.stream().filter(b -> b.id.equals(targetBarrel)).findFirst().orElse(null);
        if (barrel == null) {
            System.out.println("Error: barril " + targetBarrel + " no encontrado.");
            BeerBarrels.addSpillage(0);
            return;
        }

        while (BeerBarrels.hasActiveStudents() && !Thread.currentThread().isInterrupted()) {
            // Check interruption and active students before synchronizing
            if (!BeerBarrels.hasActiveStudents() || Thread.currentThread().isInterrupted()) {
                System.out.println("Proveedor para barril " + targetBarrel + " termina (sin estudiantes o interrumpido).");
                return;
            }

            synchronized (barrel) {
                if (barrel.currentAmount < barrel.maxCapacity) {
                    int spillage = barrel.addBeer(BEER_TO_ADD, barrels);
                    if (spillage > 0) {
                        BeerBarrels.addSpillage(spillage);
                        System.out.println("Se perdieron " + spillage + "L por desborde en el sistema desde barril " + targetBarrel + ".");
                    }
                } else {
                    System.out.println("Barril " + targetBarrel + " está lleno, proveedor esperando...");
                    try {
                        barrel.wait(100); // Short timeout to release monitor frequently
                    } catch (InterruptedException e) {
                        System.out.println("Proveedor para barril " + targetBarrel + " interrumpido, terminando...");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        System.out.println("Proveedor para barril " + targetBarrel + " termina (sin estudiantes activos).");
    }
}