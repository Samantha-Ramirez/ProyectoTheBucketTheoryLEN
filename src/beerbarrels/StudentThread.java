package beerbarrels;

import java.util.List;
import java.util.Random;

public class StudentThread implements Runnable {
    private Student student;
    private List<Barrel> barrels;
    private static final int LEGAL_AGE = 18;
    private static final int SLEEP_TIME_MS = 1000; // Tiempo de espera cuando no hay cerveza
    private final Random random = new Random();

    public StudentThread(Student student, List<Barrel> barrels) {
        this.student = student;
        this.barrels = barrels;
    }

    @Override
    public void run() {
        // Verificar si el estudiante es mayor de edad
        if (student.age < LEGAL_AGE) {
            System.out.println(student.name + " es menor de edad (" + student.age + ") y no puede pedir cerveza.");
            return;
        }

        // Continuar sirviendo cerveza mientras el estudiante tenga tickets
        while (student.tickets > 0) {
            int beersRequested = random.nextInt(student.tickets) + 1; // Random entre 1 y tickets
            int beersServed = 0;
            boolean served = false;

            // Intentar servir la cantidad solicitada desde cada barril
            for (Barrel barrel : barrels) {
                barrel.lock.lock();
                try {
                    if (barrel.currentAmount >= beersRequested) {
                        // Si hay suficiente cerveza: servir todo lo solicitado
                        barrel.currentAmount -= beersRequested;
                        student.tickets -= beersRequested;
                        beersServed = beersRequested;
                        System.out.println(student.name + " se sirvió " + beersServed + "L del barril " + barrel.id +
                                ". Litros sobrantes: " + barrel.currentAmount + "L, tickets sobrantes: " + student.tickets);
                        served = true;
                        break;
                    } else if (barrel.currentAmount > 0) {
                        // Si no hay suficiente cerveza: servir lo que está disponible
                        beersServed = Math.min(barrel.currentAmount, beersRequested);
                        barrel.currentAmount -= beersServed;
                        student.tickets -= beersServed;
                        System.out.println(student.name + " se sirvió " + beersServed + "L del barril " + barrel.id +
                                " (todo lo disponible). Litros sobrantes: " + barrel.currentAmount + "L, tickets sobrantes: " + student.tickets);
                        served = true;
                        break;
                    }
                } finally {
                    barrel.lock.unlock();
                }
            }

            // Actualizar la cantidad restante por servir en esta solicitud
            beersRequested -= beersServed;

            // Si no se sirvió todo lo solicitado o no se sirvió nada: esperar
            if (beersRequested > 0 || !served) {
                System.out.println(student.name + " necesita " + beersRequested + "L más, esperando reabastecimiento...");
                try {
                    Thread.sleep(SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    System.out.println(student.name + " interrumpido durante la espera: " + e.getMessage());
                    return;
                }
            }

            // Si no quedan tickets: el estudiante se retira
            if (student.tickets <= 0) {
                System.out.println(student.name + " no tiene más tickets y se retira de la fiesta.");
                break;
            }
        }
    }
}