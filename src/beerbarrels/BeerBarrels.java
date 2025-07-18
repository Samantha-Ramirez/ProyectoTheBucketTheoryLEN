package beerbarrels;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BeerBarrels {
    private static final AtomicInteger totalSpillage = new AtomicInteger(0);
    private static final AtomicInteger activeStudents = new AtomicInteger(0);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java beerbarrels.BeerBarrels <archivo_entrada.txt>");
            return;
        }

        if (!processInputFile(args[0])) {
            System.out.println("Error en la lectura del archivo o datos inválidos.");
        } else {
            System.out.println("Total de cerveza perdida por desborde: " + totalSpillage.get() + "L");
        }
    }

    public static void addSpillage(int amount) {
        totalSpillage.addAndGet(amount);
    }

    public static void incrementActiveStudents() {
        activeStudents.incrementAndGet();
    }

    public static void decrementActiveStudents() {
        activeStudents.decrementAndGet();
    }

    public static boolean hasActiveStudents() {
        return activeStudents.get() > 0;
    }

    public static boolean processInputFile(String fileName) {
        List<Barrel> barrels = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        List<Thread> studentThreads = new ArrayList<>();
        List<Thread> supplierThreads = new ArrayList<>();
        int numSuppliers = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            // Leer las 3 líneas de los barriles
            for (int i = 0; i < 3; i++) {
                line = br.readLine();
                lineNumber++;
                if (line == null) {
                    System.out.println("Error: archivo incompleto en línea " + lineNumber);
                    return false;
                }

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.out.println("Error: formato inválido en línea " + lineNumber);
                    return false;
                }

                String id = parts[0].trim();
                if (!id.equals("A") && !id.equals("B") && !id.equals("C")) {
                    System.out.println("Error: identificador de barril inválido en línea " + lineNumber);
                    return false;
                }

                int maxCapacity, currentAmount;
                try {
                    maxCapacity = Integer.parseInt(parts[1].trim());
                    currentAmount = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error: valores no numéricos en línea " + lineNumber);
                    return false;
                }

                if (maxCapacity < 0 || currentAmount < 0) {
                    System.out.println("Error: valores fuera de rango en línea " + lineNumber);
                    return false;
                }

                if (currentAmount > maxCapacity) {
                    currentAmount = maxCapacity;
                }

                System.out.println("Leyendo barril: ID=" + id + ", maxCapacity=" + maxCapacity + ", currentAmount=" + currentAmount);
                barrels.add(new Barrel(id, maxCapacity, currentAmount));
            }

            // Verificar si todos los barriles tienen capacidad 0
            boolean allZeroCapacity = barrels.stream().allMatch(b -> b.maxCapacity == 0);
            if (allZeroCapacity) {
                return true;
            }

            // Validar que los barriles sean 3
            if (barrels.size() != 3) {
                System.out.println("Error: los barriles deben ser exactamente 3");
                return false;
            }

            // Leer número de estudiantes
            line = br.readLine();
            lineNumber++;
            if (line == null || !line.startsWith("Estudiantes,")) {
                System.out.println("Error: formato inválido para estudiantes en línea " + lineNumber);
                return false;
            }

            int numStudents;
            try {
                numStudents = Integer.parseInt(line.split(",")[1].trim());
                // Suposición: se puede 0 estudiantes
                if (numStudents < 0) {
                    System.out.println("Error: número de estudiantes inválido en línea " + lineNumber);
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: número de estudiantes no numérico en línea " + lineNumber);
                return false;
            }

            System.out.println("Leyendo número de estudiantes: " + numStudents);

            // Leer número de proveedores
            line = br.readLine();
            lineNumber++;
            if (line == null || !line.startsWith("Proveedores,")) {
                System.out.println("Error: formato inválido para proveedores en línea " + lineNumber);
                return false;
            }

            try {
                numSuppliers = Integer.parseInt(line.split(",")[1].trim());
                // Suposición: se puede 0 proveedores
                if (numSuppliers < 0) {
                    System.out.println("Error: número de proveedores inválido en línea " + lineNumber);
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: número de proveedores no numérico en línea " + lineNumber);
                return false;
            }

            System.out.println("Leyendo número de proveedores: " + numSuppliers);

            // Leer datos de estudiantes
            for (int i = 0; i < numStudents; i++) {
                line = br.readLine();
                lineNumber++;
                if (line == null) {
                    System.out.println("Error: archivo incompleto para estudiantes en línea " + lineNumber);
                    return false;
                }

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.out.println("Error: formato inválido para estudiante en línea " + lineNumber);
                    return false;
                }

                String name = parts[0].trim();
                if (name.isEmpty()) {
                    System.out.println("Error: nombre de estudiante vacío en línea " + lineNumber);
                    return false;
                }

                int age, tickets;
                try {
                    age = Integer.parseInt(parts[1].trim());
                    tickets = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error: valores no numéricos para estudiante en línea " + lineNumber);
                    return false;
                }

                if (age <= 0 || tickets < 0) {
                    System.out.println("Error: valores fuera de rango para estudiante en línea " + lineNumber);
                    return false;
                }

                System.out.println("Leyendo estudiante: nombre=" + name + ", edad=" + age + ", tickets=" + tickets);
                students.add(new Student(name, age, tickets));
            }

            // Disparar hilos para estudiantes
            for (Student student : students) {
                if (student.age >= 18) {
                    incrementActiveStudents();
                }
                Thread studentThread = new Thread(new StudentThread(student, barrels) {
                    @Override
                    public void run() {
                        super.run();
                        decrementActiveStudents();
                    }
                });
                studentThreads.add(studentThread);
                System.out.println("Comienza thread: " + student.name);
                studentThread.start();
            }

            // Esperar a que al menos un estudiante esté activo antes de iniciar proveedores
            while (activeStudents.get() == 0 && !students.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println("Error al esperar estudiantes activos: " + e.getMessage());
                    return false;
                }
            }

            // Disparar hilos para proveedores
            for (int i = 0; i < numSuppliers; i++) {
                String targetBarrel = (i % 2 == 0) ? "A" : "C";
                Thread supplierThread = new Thread(new Supplier(barrels, targetBarrel, i));
                supplierThreads.add(supplierThread);
                supplierThread.start();
            }

            // Esperar a que todos los hilos de estudiantes terminen
            for (Thread thread : studentThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("Error al esperar hilos de estudiantes: " + e.getMessage());
                    return false;
                }
            }

            // Interrumpir hilos de proveedores
            for (Thread thread : supplierThreads) {
                thread.interrupt();
            }

            // Esperar a que todos los hilos de proveedores terminen
            for (Thread thread : supplierThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("Error al esperar hilos de proveedores: " + e.getMessage());
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
            return false;
        }
    }
}