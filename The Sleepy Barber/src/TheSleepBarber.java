import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TheSleepBarber extends Application {
    public static final int CHAIRS = 5;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = createPane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("The Sleepy Barber");
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.getIcons().add(new Image("icon.png"));
        primaryStage.show();
        
        
        
        
        
        BarberShop barberShop = new BarberShop();

        Thread barberThread = new Barber(barberShop);
        barberThread.start();

        Thread customerGeneratorThread = new Thread(new CustomerGenerator(barberShop));
        customerGeneratorThread.start();
    }

    private Pane createPane() {
        int width = 1300;
        Pane pane = new Pane();
        pane.setPrefSize(width, 650);
        Image backgroundImage = new Image("background.png");
        BackgroundSize backgroundSize = new BackgroundSize(1100, 650, false, false, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        pane.setBackground(new Background(background));

        return pane;
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
             chairs.acquire();
            // System.out.println("Customer " + id + " is getting a haircut.");
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
