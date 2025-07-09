package beerbarrels;

import java.util.List;

public class Supplier implements Runnable {
    private List<Barrel> barrels;
    private String targetBarrel;
    private int id;
    private final int BEER_TO_ADD = (int) ((Math.random() * 10) + 1);

    public Supplier(List<Barrel> barrels, String targetBarrel, int id) {
        this.barrels = barrels;
        this.targetBarrel = targetBarrel;
        this.id = id;
    }

    @Override
    public void run() {
        // Validar barril objetivo (solo A o C permitido)
        if (!targetBarrel.equals("A") && !targetBarrel.equals("C")) {
            System.out.println("Error: proveedor " + id + " intentó añadir cerveza al barril " + targetBarrel + ", solo A o C permitidos.");
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
            boolean available = false;
            for (Barrel b : barrels){
                if(b.currentAmount < b.maxCapacity){
                    available =true;
                    break;
                }
            }
            
            if (available) {
                int spillage = barrel.addBeer(BEER_TO_ADD, barrels, id);
                if (spillage > 0) {
                    BeerBarrels.addSpillage(spillage);
                    System.out.println("Se perdieron " + spillage + "L por desborde en el sistema desde barril " + targetBarrel + ".");
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Proveedor " + id + " para barril " + targetBarrel + " finalizó su jornada");
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("Proveedor " + id + " para barril " + targetBarrel + " finalizó su jornada");
    }
}
