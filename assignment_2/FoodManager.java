/** 
 * -------------------------------------- 
 * CS205 Assignment 2 : Java Concurrency
 * --------------------------------------
 * Name               : Kang Chin Shen
 * Matriculation ID   : 01412921
 * --------------------------------------
 */


/** 
 * Import packages for reading from or writing to log file
 */
import java.io.*;
import java.util.*;
import java.nio.file.*;


/**
 * Main thread -> FoodManager
 */
public class FoodManager {

    // Set the log file path
    static final String fileName = "log.txt";

    // Store N,M,K,W,X,Y,Z as attributes
    static int hotdogs;
    static int burgers;
    static int capacity;
    static int hotdogMakers;
    static int burgerMakers;
    static int hotdogPackers;
    static int burgerPackers;

    // Counters, buffer, and locks
    volatile static int packedHotdogsCount = 0;
    volatile static int packedBurgersCount = 0;
    volatile static int cookedBurgersCount = 0;
    volatile static int cookedHotdogsCount = 0;
    volatile static Buffer buffer = null;
    static Object packerLock = new Object();


    /**
     * Clear records of previous program execution &
     * Create the log file if it does not exist
     */
    static void clearContent() {

        try (PrintWriter pw = new PrintWriter(fileName)) {}
        catch (Exception e) { }
    }

    /**
     * Write records to log file (in APPEND mode)
     * @param text text is the record to be written to the file
     */
    static void writeFile(String text) {

        try {
            Files.write(Paths.get(fileName), text.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) { }
    }


    /**
     * Set attributes' values
     */
    static void setAttributes(String arr[]) {

        hotdogs = Integer.parseInt(arr[0]);
        burgers = Integer.parseInt(arr[1]);

        capacity = Integer.parseInt(arr[2]);

        hotdogMakers = Integer.parseInt(arr[3]);
        burgerMakers = Integer.parseInt(arr[4]);

        hotdogPackers = Integer.parseInt(arr[5]);
        burgerPackers = Integer.parseInt(arr[6]);
    }


    /**
     * Display attributes' values in the log file
     */
    static void showAttributes() {

        writeFile(
            "hotdogs:" + hotdogs + "\n" +
            "burgers:" + burgers + "\n" +

            "capacity:" + capacity + "\n" +

            "hotdog makers:" + hotdogMakers + "\n" +
            "burger makers:" + burgerMakers + "\n" +

            "hotdog packers:" + hotdogPackers + "\n" +
            "burger packers:" + burgerPackers + "\n" 
        );
    }


    /**
     * Write summary statements for each machine type to log file
     * @param arr arr is an array that stores specific type of machines
     * @param machine machine is the machine type
     * @param action action is either 'makes' or 'packs' 
     */
    static void logSummaryStatement(int[] arr, String machine, String action) {

        for(int i = 0; i < arr.length; i++) {
           writeFile(machine + i + " " + action + " " + arr[i] + "\n");
        }
    }


    /**
     * Read the log file and compute summary data
     */
    static void showSummary() {

        int[] bm = new int[burgerMakers];
        int[] hm = new int[hotdogMakers];
        int[] bc = new int[burgerPackers];
        int[] hc = new int[hotdogPackers];

        // Skip first 7 lines in the log file -> for showing attributes
        int skip = 7;

        File file = new File(fileName);

        try(Scanner sc = new Scanner(file)) {
            while(sc.hasNextLine()) {
                if(skip == 0) {
                    String line = sc.nextLine();

                    String[] words = line.split(" ");

                    String entity = "" + words[0].charAt(0) + words[0].charAt(1);

                    if (entity.equals("bm")) {
                        bm[Integer.parseInt(words[0].substring(2))]++;
                    } else if (entity.equals("hm")) {
                        hm[Integer.parseInt(words[0].substring(2))]++;
                    } else if (entity.equals("bc")) {
                        bc[Integer.parseInt(words[0].substring(2))]++;
                    } else if (entity.equals("hc")) {
                        hc[Integer.parseInt(words[0].substring(2))]+=2;
                    }
                    
                } else { skip--; }
            }
        } catch (FileNotFoundException e) { }

        writeFile("summary:\n");
        logSummaryStatement(bm, "bm", "makes");
        logSummaryStatement(hm, "hm", "makes");
        logSummaryStatement(bc, "bc", "packs");
        logSummaryStatement(hc, "hc", "packs");
    }


    /**
     * Simulate work for machine
     * @param n_seconds n_seconds is the number of seconds (roughly) this method runs
     */
    static void goWork(int n_seconds) {

        for (int i = 0; i < n_seconds; i++) {
            long n = 300000000;
            while (n > 0) {
                n--;
            }
        }
    }


    /**
     * Start each machine and place them in correct position in the array
     * @param threads is an array that stores all the threads
     * @param size is the size of the threads array
     */
    static void startMachines(Thread[] threads, int size) {

        for(int i = 0; i < size; i++) {

            if(0 <= i && i < hotdogMakers) {
                threads[i] = new Thread(new MakingMachine("hm" + i, "hotdog"));
            } else if (i < burgerMakers + hotdogMakers) {
                threads[i] = new Thread(new MakingMachine("bm" + (i - hotdogMakers), "burger"));
            } else if (i < hotdogPackers + burgerMakers + hotdogMakers) {
                threads[i] = new Thread(new PackingMachine("hc" + (i - hotdogMakers - burgerMakers), "hotdog"));
            } else {
                threads[i] = new Thread(new PackingMachine("bc" + (i - hotdogMakers - burgerMakers - hotdogPackers), "burger"));
            }

            threads[i].start();
        }
    }

    /**
     * Attributes validation and error handling
     * @return false if any of the attributes' values is incorrect or else return true
     */
    static boolean isAttributesValid() {

        if (hotdogs % 2 != 0) {

            writeFile("Error : Number of hotdogs must be even");
            return false;

        } else if (capacity <= 0) {

            writeFile("Error : Capacity must be greater than zero");
            return false;

        } else if (hotdogs > 0 && (hotdogMakers < 1 || hotdogPackers < 1)) {

            writeFile(String.format("Error : %d required hotdogs require at least 1 hotdog maker and 1 hotdog packer", hotdogs));
            return false;

        } else if (burgers > 0 && (burgerMakers < 1 || burgerPackers < 1)) {

            writeFile(String.format("Error : %d required burgers require at least 1 burger maker and 1 burger packer", burgers));
            return false;
        }

        return true;
    }

    public static void main(String[] args) {

        System.out.println("Program is executing ...");
        
        clearContent();
        
        setAttributes(args);
        
        showAttributes();

        if (!isAttributesValid()) return;

        buffer = new Buffer(capacity);

        int size = hotdogMakers + burgerMakers + hotdogPackers + burgerPackers;
        Thread[] threads = new Thread[size];
        startMachines(threads, size);

        for(int i = 0; i < size; i++) {
            try { threads[i].join(); } catch (InterruptedException e) { }
        }

        showSummary();

        System.out.println("Program ended successfully!");
    }
}

/**
 * MakingMachine class of type either hotdog or burger
 */
class MakingMachine implements Runnable {

    // Making Machine ID -> either 'hm<number>' or 'bm<number>'
    String id;

    // Type of the machine (either hotdog or burger)
    String type;

    MakingMachine(String id, String type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public void run() {
        Food food = null;
        switch (type) {
            case "hotdog":
                while (FoodManager.cookedHotdogsCount < FoodManager.hotdogs) {

                    // Hotdog making machine takes 3s to make a hotdog
                    FoodManager.goWork(3);

                    // Check if the last hotdog has been made
                    if (FoodManager.cookedHotdogsCount >= FoodManager.hotdogs) break;

                    food = new Hotdog(FoodManager.cookedHotdogsCount++, id); 

                    // Hotdog making machine takes 1s to send a hotdog to the pool
                    FoodManager.goWork(1);

                    FoodManager.buffer.put(food);
                }
                break;

            case "burger":
                while (FoodManager.cookedBurgersCount < FoodManager.burgers) {

                    // Burger making machine takes 8s to make a burger
                    FoodManager.goWork(8);

                    // Check if the last burger has been made
                    if (FoodManager.cookedBurgersCount >= FoodManager.burgers) break;

                    food = new Burger(FoodManager.cookedBurgersCount++, id); 

                    // Burger making machine takes 1s to send a burger to the pool
                    FoodManager.goWork(1);

                    FoodManager.buffer.put(food); 
                }
                break;
        }
    }
}



/**
 * PackingMachine class of type either hotdog or burger
 */
class PackingMachine implements Runnable {

    // Packing Machine ID -> either 'hc<number>' or 'bc<number>'
    String id;

    // Type of the machine (either hotdog or burger)
    String type;

    // Number of food the machine has taken
    int numTaken = 0;

    // Foods that the machine has taken
    Food[] foods = new Food[2];

    PackingMachine(String id, String type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public void run() {

        outer: while (FoodManager.packedBurgersCount < FoodManager.burgers || FoodManager.packedHotdogsCount < FoodManager.hotdogs) {

            synchronized (FoodManager.packerLock) {
                while (FoodManager.buffer.checkFirstItem() == null || !FoodManager.buffer.checkFirstItem().equals(this.type)) {

                    // If all foods have already been packed, stop the machine
                    if (FoodManager.packedBurgersCount >= FoodManager.burgers && FoodManager.packedHotdogsCount >= FoodManager.hotdogs)
                        break outer;

                    try { FoodManager.packerLock.wait(); } catch (Exception e) { }
                }

                // Both hotdog packing machine and burger packing machine take 1s to get the food
                FoodManager.goWork(1);

                switch (type) {
                    case "hotdog":
                        // If hotdog packing machine takes 0 or 1 hotdog
                        if (numTaken < 2) {

                            foods[numTaken++] = FoodManager.buffer.get();

                            // If hotdog packing machine already took 1 hotdog and first food in the buffer is hotdog
                            if (numTaken < 2 && FoodManager.buffer.checkFirstItem().equals("hotdog")) {
                                
                                foods[numTaken++] = FoodManager.buffer.get();
                            } else {

                                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                            }
                        }

                        break;

                    case "burger":
                        foods[numTaken++] = FoodManager.buffer.get();
                        break;
                }

                // Notify blocked packers in packerLock
                FoodManager.packerLock.notifyAll();
            }

            // Both hotdog packing machine and burger packing machine take 2s to pack the food
            FoodManager.goWork(2); 

            switch (type) {
                case "hotdog":

                    if (numTaken == 2) {
                        FoodManager.writeFile(String.format("%s gets %s id:%d from %s and id:%d from %s\n", 
                            id, "hotdog", 
                            foods[0].getId(), 
                            foods[0].getMakingMachineId(), 
                            foods[1].getId(), 
                            foods[1].getMakingMachineId()));

                        FoodManager.packedHotdogsCount += 2; 

                        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                        numTaken = 0; 
                    }

                    break;

                case "burger":

                    FoodManager.writeFile(String.format("%s gets %s id:%d from %s\n", 
                        id, "burger", 
                        foods[0].getId(), 
                        foods[0].getMakingMachineId()));

                    FoodManager.packedBurgersCount += 1;

                    numTaken = 0; 

                    break;
            }
        }

        // Notify blocked packers in buffer
        synchronized (FoodManager.buffer) { FoodManager.buffer.notifyAll(); }
    }
}


/**
 * Buffer Class
 */
class Buffer{

    private Food[] buffer;
    public int itemCount = 0;
    private int front = 0, back = 0;


    /**
     * Contructor
     * @param size size is the buffer size
     */
    Buffer(int size) {
        buffer = new Food[size];
    }

    /**
     * Put food into the buffer 
     * @param food is either of type Hotdog or Burger
     */
    synchronized void put(Food food) {

        while(itemCount == buffer.length) {
            try { this.wait(); } catch (InterruptedException e) { }
        }

        buffer[back] = food;

        back = (back + 1) % buffer.length;

        itemCount++;

        if(food instanceof Hotdog) {
            FoodManager.writeFile(String.format("%s puts %s id:%d\n", food.getMakingMachineId(), "hotdog", food.getId())); 
        } else {
            FoodManager.writeFile(String.format("%s puts %s id:%d\n", food.getMakingMachineId(), "burger", food.getId())); 
        }

        this.notifyAll();
    }

    /**
     * Get food from the buffer
     * @return food either of type Hotdog or Burger
     */
    synchronized Food get() { 

        while(itemCount == 0) {
            try { this.wait(); } catch (InterruptedException e) { }
        }

        Food food = buffer[front];

        buffer[front] = null;

        front = (front + 1) % buffer.length;

        itemCount--;

        this.notifyAll();

        return food;
    }

    /**
     * Check the first item of the buffer
     * @return the type of first item in String and null if there is no item in the buffer
     */
    synchronized String checkFirstItem() {
        
        while(buffer[front] == null &&
             (FoodManager.packedBurgersCount < FoodManager.burgers ||
              FoodManager.packedHotdogsCount < FoodManager.hotdogs)) {

                try { this.wait(); } catch (InterruptedException e) { }
        }

        if (buffer[front] == null) { return null; }
            
        return buffer[front] instanceof Hotdog ? "hotdog" : "burger";
        
    }
}


/**
 * Food Class
 */
class Food {

    // Food ID corresponding manufacturing machine ID
    private int id;
    private String makingMachineId;
    
    Food(int id, String machineId) {
        this.id = id;
        this.makingMachineId = machineId;
    }

    int getId() {
        return id;
    }

    String getMakingMachineId() {
        return makingMachineId;
    }
}


/**
 * Hotdog class extends Food class
 */
class Hotdog extends Food {
    
    Hotdog(int id, String machineId){
        super(id, machineId);
    }
}

/**
 * Burger class extends Food class
 */
class Burger extends Food{

    Burger(int id, String machineId){
        super(id, machineId);
    }
}