# ProyectoTheBucketTheoryLEN

Nuestro proyecto consta de 5 archivos Java que describen el funcionamiento del mismo

#Barrel

- public class Barrel: Crea la clase barrel y la inicializa con un id, capacidad y maxima capacidad.
  
- public int addBeer(int amount, List<Barrel> barrels, List<String> visited, int supplier): Luego creamos la funcioón addBeer que como parametros toma una cantidad, una lista de Barriles, una lista de barriles visitados y un entero que indica el codigo del proveedor que lo llama.

  Al llamarse se incluye el barril en la lista de visitados para evitar un bucle infinito entre desbordes al agregar cerveza.
  -  synchronized (this) : se usa una condición de sincronización para asegurarnos que un solo proceso interactue con cada barril a la vez a traves de la función. Si la capacidad actual + la cantidad a agregar es menor a la capacidad maxima del barril simplemente se suma y se usa la función notifyAll() para liberar el barril para que participen otros procesos.
    En el caso de que esto no se cumpla, existe un desbordamiento, por lo que se chequea en que barril nos encontramos para transmitirlo al barril adyacente correspondiente. Esto se hace obteniendo el objeto del Barril adyacente siempre y cuando no este en la lista de visitados. Luego se llama la funcion addBeer nuevamente pero agregando el desborde a este barril adyacente.
  Por ultimo se liberan todos los barriles con notifyAll y se retorna el spillage que es el desborde que no pudo transferirse entre los barriles

- public synchronized int consumeBeer(int amount): Esta función regula el pedido de bebida por parte de los estudiantes, de igual forma esta sincronizada para asefurarnos que varios procesos no la apliquen a la vez. Cuando hay suficiente cantidad de cerveza en barril para servir el pedido resta esta cantidad y devuelve lo que servido, si este no es el caso deja el barril en 0 y retorna la cantidad de cerveza que existia originalmente en el barril.

#BeerBarrels

- public class BeerBarrels: Crea la clase de la lista de barriles.
  Dentro de esta, tenemos un entero que establece el total de perdidas por desborde con sun función para alterarlo y tambien otro entero con la cantidad de estudiantes activos y su función para incrementarlo y decrementarlo.

-  public static boolean processInputFile(String fileName): Esta función lee los datos del archivo de texto y rellena nuestra lista de barriles, lista de estudiantes, lista de hilos de proveedores y lista de hilos de estudiantes.

-  for (Student student : students) : esta sección dispara la ejecución de los hilos de estudiantes creados y luego de que se cierra el hilo decrementa la cantidad de estudiantes activos.

  - while (activeStudents.get() == 0 && !students.isEmpty()): Esta sección asegura que exista al menos un estudiante activo antes de iniciar los hilos de los proveedores

- for (int i = 0; i < numSuppliers; i++): esta sección dispara los hilos de los proveedores y les asigno un barril de target que es el cual son responsables de monitorear

- for (Thread thread : studentThreads) y for (Thread thread : supplierThreads): espera que todos los los hilos de estudiante y de proveedor terminen.

#Student

- public class Student: Crea e inicializa la clase estudiante con un string de nombre, una edad y una cantidad de tickets

#StudentThread

- public class StudentThread implements Runnable: Crea el proceso estudiante el cual toma los datos de un estudiante, nuestra lista de barrilles (recurso critico usado por varios hilos) y un cantidad random de cerveza que tomaran cada vez que busquen cerveza en los 

-  if (student.age < LEGAL_AGE): Verifica que el estudiante es mayor de 18 años

-  while (student.tickets > 0 && !Thread.currentThread().isInterrupted()): Mientras el estudiante tenga tickets y el hilo este interrumpido el estudiante va a pedir un numero random de vasos de cerveza entre 1 y los tickets que tiene.

-  for (Barrel barrel : barrels): El estudiante intenta tomar cerveza de todos los barriles.

   - synchronized (barrel): aplica una condición de sincronización para que otros hilos no puedan interactuar con el mismo objeto.
  
   -  int amountServed = barrel.consumeBeer(beersRequested): La cantidad servida es igual a la salida de la funcion consumeBeer en el barril.
       - if (amountServed > 0) : Si el estudiante logró servirse cerveza del barril se le restan sus vasos del ticket, se marca como servido, se libera el objeto con notifyALL y termina el bucle
       - if (amountServed < beersRequested): Si no hay suficiente cerveza en el barril para servir al estudiante la cantidad que pidio, se libera el barril con un wait para que el hilo del proveedor pueda rellenarlo.
         
  - if (!served): Si no hay suficiente cerveza para servir vasos en ningun barril, el hilo dentendra su ejecución temporalmente a la espera de que se llenen los barriles
    
  - if (student.tickets <= 0): Cuando al estudiante se le acaban los tickets se termina el proceso.

#Supplier

- public class Supplier implements Runnable: Crea el proceso de los proveedores el cual toma nuestra lista de barriles (recurso critico usado por varios hilos), un barril target, un id del proveedor y un numero random de cerveza que agregará en cada momemento.

- if (!targetBarrel.equals("A") && !targetBarrel.equals("C")): Evita que se agregue cerveza al barril B

- Barrel barrel = barrels.stream().filter(b -> b.id.equals(targetBarrel)).findFirst().orElse(null): Se asegura de que el barril target exista en la lista.

- while (BeerBarrels.hasActiveStudents() && !Thread.currentThread().isInterrupted()): El proceso se ejecuta mientras hayan estudiantes activos y no se interrumpa

  - for (Barrel b : barrels): verifica que existan barriles no llenos al completo.

  - int spillage = barrel.addBeer(BEER_TO_ADD, barrels, id): Intenta llenar el barril target y calcula la perdida de desbordamiento luego del proceso. Luego se detiene temporalmente y continua.
 
