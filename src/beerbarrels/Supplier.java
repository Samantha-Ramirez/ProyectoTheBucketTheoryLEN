package beerbarrels;

import java.util.List;

public class Supplier implements Runnable {
    private List<Barrel> barrels;
    private String targetBarrel;

    public Supplier(List<Barrel> barrels, String targetBarrel) {
        this.barrels = barrels;
        this.targetBarrel = targetBarrel;
    }

    @Override
    public void run() {
        // Lógica para agregar cerveza al barril A o C
        // Se implementará en la Parte 3
    }
}