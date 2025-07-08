package beerbarrels;

import java.util.List;
import java.util.Random;

public class StudentThread implements Runnable {
    private Student student;
    private List<Barrel> barrels;
    private static final int LEGAL_AGE = 18;
    private final Random random = new Random();

    public StudentThread(Student student, List<Barrel> barrels) {
        this.student = student;
        this.barrels = barrels;
    }

    @Override
    public void run() {
        // Verificar si el estudiante es mayor de edad
        if (student.age < LEGAL_AGE) {
            System.out.println(student.name + " es menor de edad (" + student.age + ")");
            return;
        }

        // Continuar sirviendo cerveza mientras el estudiante tenga tickets
        while (student.tickets > 0) {
            int beersRequested = random.nextInt(student.tickets) + 1; // Random entre 1 y tickets
            int beersServed = 0;
            boolean served = false;

            // Intentar servir la cantidad solicitada desde cada barril
            for (Barrel barrel : barrels) {
                synchronized (barrel) {
                    int amountServed = barrel.consumeBeer(beersRequested);
                    if (amountServed > 0) {
                        student.tickets -= amountServed;
                        beersServed = amountServed;
                        served = true;
                        break;
                    }
                    // Si no hay suficiente cerveza: esperar reabastecimiento
                    if (amountServed < beersRequested) {
                        System.out.println(student.name + " necesita " + (beersRequested - amountServed) + "L más en barril " + barrel.id + ", esperando reabastecimiento...");
                        try {
                            barrel.wait();
                        } catch (InterruptedException e) {
                            System.out.println(student.name + " interrumpido durante la espera: " + e.getMessage());
                            return;
                        }
                    }
                }
            }

            // Actualizar la cantidad restante por servir en esta solicitud
            beersRequested -= beersServed;

            // Si no se sirvió todo lo solicitado o no se sirvió nada: intentar otro barril
            if (beersRequested > 0 || !served) {
                if (!served) {
                    System.out.println(student.name + " no encontró cerveza disponible, intentando otro barril...");
                }
            }

            // Si no quedan tickets: el estudiante se retira
            if (student.tickets <= 0) {
                System.out.println(student.name + " no tiene más tickets y se retira de la fiesta");
                break;
            }
        }
    }
}