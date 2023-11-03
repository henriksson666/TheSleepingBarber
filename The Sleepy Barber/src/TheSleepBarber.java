import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class TheSleepBarber {
    public static final int CHAIRS = 5;
    public static final int NUM_CUSTOMERS = 10;

    public static void main(String[] args) {
        BarberShop barberShop = new BarberShop();

        Thread barberThread = new Barber(barberShop);
        barberThread.start();

        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            Thread customerThread = new Customer(barberShop, i);
            customerThread.start();
        }
    }
}

class BarberShop {
    private Semaphore customers = new Semaphore(0);
    private Semaphore barbers = new Semaphore(0);
    private Semaphore mutex = new Semaphore(1);
    private Queue<Integer> waitingCustomers = new LinkedList<>(); // FIFO queue

    public void barber() throws InterruptedException {
        while (true) {
            customers.acquire(); // Sleep if there are no customers
            mutex.acquire();
            int customerId = waitingCustomers.poll(); // Get the first customer in the queue
            mutex.release();

            barbers.release();

            System.out.println("Barber is cutting hair for customer " + customerId);
            Thread.sleep(2000); // Simulate hair cutting
        }
    }

    public void customer(int id) throws InterruptedException {
        mutex.acquire();
        if (waitingCustomers.size() < TheSleepBarber.CHAIRS) {
            waitingCustomers.offer(id); // Join the end of the queue
            customers.release();
            mutex.release();
            barbers.acquire(); // Sleep if no barbers are available

            System.out.println("Customer " + id + " is getting a haircut.");
        } else {
            mutex.release();
            System.out.println("Customer " + id + " is leaving because the shop is full.");
        }
    }
}

class Barber extends Thread {
    private BarberShop shop;

    public Barber(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            shop.barber();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Customer extends Thread {
    private BarberShop shop;
    private int id;

    public Customer(BarberShop shop, int id) {
        this.shop = shop;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            shop.customer(id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
