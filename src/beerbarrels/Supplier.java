package beerbarrels;

import java.util.List;

public class Supplier implements Runnable {
    private List<Barrel> barrels;
    private String targetBarrel;
    private static final int BEER_TO_ADD = 5; // Cantidad de cerveza a añadir por intento

    public Supplier(List<Barrel> barrels, String targetBarrel) {
        this.barrels = barrels;
        this.targetBarrel = targetBarrel;
    }

    @Override
    public void run() {
        // Validar barril objetivo (solo A o C permitido)
        if (!targetBarrel.equals("A") && !targetBarrel.equals("C")) {
            System.out.println("Error: proveedor intentó añadir cerveza al barril " + targetBarrel + ", solo A o C permitidos.");
            BeerBarrels.addSpillage(0); // Asegura que el proveedor contribuya a la terminación
            return;
        }

        // Encontrar el barril objetivo
        Barrel barrel = barrels.stream().filter(b -> b.id.equals(targetBarrel)).findFirst().orElse(null);
        if (barrel == null) {
            System.out.println("Error: barril " + targetBarrel + " no encontrado.");
            BeerBarrels.addSpillage(0);
            return;
        }

        // Continuar añadiendo cerveza mientras haya estudiantes activos
        while (BeerBarrels.hasActiveStudents()) {
            synchronized (barrel) {
                if (!BeerBarrels.hasActiveStudents()) {
                    break; // Salir si no hay estudiantes activos
                }
                if (barrel.currentAmount < barrel.maxCapacity) {
                    // Añadir cerveza y manejar desborde
                    int spillage = barrel.addBeer(BEER_TO_ADD, barrels);
                    if (spillage > 0) {
                        BeerBarrels.addSpillage(spillage);
                        System.out.println("Se perdieron " + spillage + "L por desborde en el sistema.");
                    }
                } else {
                    // Barril lleno, esperar consumo
                    System.out.println("Barril " + targetBarrel + " está lleno, proveedor esperando...");
                    try {
                        barrel.wait();
                    } catch (InterruptedException e) {
                        System.out.println("Proveedor para barril " + targetBarrel + " interrumpido, terminando...");
                        return;
                    }
                }
            }
        }
    }
}