import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class TheSleepBarber {
    public static final int CHAIRS = 5;

    public static void main(String[] args) {
        BarberShop barberShop = new BarberShop();

        Thread barberThread = new Barber(barberShop);
        barberThread.start();

        Thread customerGeneratorThread = new Thread(new CustomerGenerator(barberShop));
        customerGeneratorThread.start();
    }
}

class BarberShop {
    private Semaphore customers = new Semaphore(0);
    private Semaphore chairs = new Semaphore(TheSleepBarber.CHAIRS);
    private Semaphore mutex = new Semaphore(1);
    private Queue<Integer> waitingCustomers = new LinkedList<>();

    public void barber() throws InterruptedException {
        while (true) {
            customers.acquire();

            if (waitingCustomers.isEmpty()) {
                System.out.println("Barber is sleeping.");
            }

            mutex.acquire();
            int customerId = waitingCustomers.poll();
            mutex.release();
            chairs.release();

            System.out.println("Barber is cutting hair for customer " + customerId);
            Thread.sleep(5000);
        }
    }

    public void customer(int id) throws InterruptedException {
        System.out.println("Customer " + id + " is entering the shop.");

        mutex.acquire();
        if (waitingCustomers.size() < TheSleepBarber.CHAIRS) {
            waitingCustomers.offer(id);
            customers.release();
            mutex.release();
            //chairs.acquire();
            //System.out.println("Customer " + id + " is getting a haircut.");
        } else {
            System.out.println("Customer " + id + " is leaving because the shop is full.");
            mutex.release();
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

class CustomerGenerator extends Thread {
    private BarberShop shop;
    private int customerId = 1;

    public CustomerGenerator(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            while (true) {
                shop.customer(customerId);
                customerId++;
                int randomDelay = ThreadLocalRandom.current().nextInt(1, 4);
                Thread.sleep(randomDelay * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
