package beerbarrels;

import java.util.List;

public class StudentThread implements Runnable {
    private Student student;
    private List<Barrel> barrels;

    public StudentThread(Student student, List<Barrel> barrels) {
        this.student = student;
        this.barrels = barrels;
    }

    @Override
    public void run() {
        // Lógica para servir vasos de cerveza
        // Se implementará en la Parte 3
    }
}