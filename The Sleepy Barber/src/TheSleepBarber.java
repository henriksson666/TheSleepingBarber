import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TheSleepBarber extends Application {
    public static final int CHAIRS = 5;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean[] isBarberRunning = { true };
        
        Pane root = createPane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("The Sleepy Barber");
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.getIcons().add(new Image("icon.png"));
        primaryStage.show();

        VBox permanentControlVBox = createPermanentControlVBox();
        root.getChildren().add(permanentControlVBox);
        Button resetButton = createReseButton("Play Animation");
        Button togglePlayPauseBarber = createReseButton("Play/Pause");
        Button togglePlayPauseCustomer = createReseButton("Play/Pause");
        VBox barberControlVBox = createControlVBox("Barber");
        VBox customerControlVBox = createControlVBox("Customer");
        VBox informationControlVBox = createInformationControlVBox();
        Slider barberSlider = creatSlider(0, 10, 2);
        Slider customerSlider = creatSlider(0, 10, 2);
        Text barberSliderText = createText("Barber Speed: ");
        Text customerSliderText = createText("Customer Speed: ");
        Text waitingRoomCustomersText = createText("Waiting Room Customers: ");
        Text barberStatusText = createText("Barber Status: ");
        Text customerStatusText = createText("Customer Status: ");
        Text servedCustomersText = createText("Served Customers: ");
        Text lostCustomersText = createText("Lost Customers: ");
        Text customersSatistactionText = createText("Customers Satistaction: ");
        Text customerInsatistactionText = createText("Customers Insatistaction: ");
        informationControlVBox.getChildren().addAll(barberSliderText, barberSlider, customerSliderText, customerSlider,
                waitingRoomCustomersText, barberStatusText, customerStatusText, servedCustomersText, lostCustomersText,
                customersSatistactionText, customerInsatistactionText);

        barberControlVBox.getChildren().addAll(barberSlider, togglePlayPauseBarber);
        customerControlVBox.getChildren().addAll(customerSlider, togglePlayPauseCustomer);
        permanentControlVBox.getChildren().addAll(resetButton, barberControlVBox, customerControlVBox);
        root.getChildren().add(informationControlVBox);

        BarberShop barberShop = new BarberShop();
        Thread barberThread = new Barber(barberShop);
        Thread customerGeneratorThread = new Thread(new CustomerGenerator(barberShop));

        resetButton.setOnAction(event -> {
            resetButton.setText("Reset");
            togglePlayPauseBarber.setText("Pause");
            togglePlayPauseCustomer.setText("Pause");
            barberThread.start();
            customerGeneratorThread.start();
        });

        togglePlayPauseBarber.setOnAction(event -> {
            if (togglePlayPauseBarber.getText().equals("Play")) {
                togglePlayPauseBarber.setText("Pause");
                isBarberRunning[0] = true;
                synchronized (isBarberRunning) {
                    isBarberRunning.notify();
                }
            } else {
                togglePlayPauseBarber.setText("Play");
                isBarberRunning[0] = false;
            }
        });

        togglePlayPauseCustomer.setOnAction(event -> {
            if (togglePlayPauseCustomer.getText().equals("Play")) {
                togglePlayPauseCustomer.setText("Pause");
                customerGeneratorThread.resume();
            } else {
                togglePlayPauseCustomer.setText("Play");
                customerGeneratorThread.suspend();
            }
        });
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

    private VBox createPermanentControlVBox() {
        int xProperty = 1100;
        VBox vBox = new VBox();
        vBox.translateXProperty().set(xProperty);
        vBox.translateYProperty().set(0);
        vBox.setPrefSize(200, 100);
        vBox.setStyle(
                "-fx-background-color: radial-gradient(radius 180%, #f99832, #fdc88e, #fdc88e); -fx-padding: 5px; -fx-spacing: 5; -fx-alignment: center;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(-1.0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.BLACK);
        vBox.setEffect(shadow);

        return vBox;
    }

    private Text createText(String string) {
        Text text = new Text(string);
        text.setStyle("-fx-fill: #000; -fx-font-size: 15px; -fx-font-weight: bold;");

        return text;
    }

    private VBox createInformationControlVBox() {
        int xProperty = 1100;
        VBox vBox = new VBox();
        vBox.translateXProperty().set(xProperty);
        vBox.translateYProperty().set(217);
        vBox.setPrefSize(200, 433);
        vBox.setStyle(
                "-fx-background-color: radial-gradient(radius 180%, #f99832, #fdc88e, #fdc88e); -fx-padding: 5px; -fx-spacing: 10; -fx-alignment: center;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(-1.0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.BLACK);
        vBox.setEffect(shadow);

        return vBox;
    }

    private VBox createControlVBox(String string) {
        VBox vBox = new VBox();
        vBox.setPrefSize(200, 100);
        vBox.setStyle(
                "-fx-background-color: rgba(245, 245, 220, 0.7); -fx-background-radius: 2px; -fx-alignment: center; -fx-padding: 2px;");

        Text text = new Text(string);
        text.setStyle("-fx-fill: #000; -fx-font-size: 15px; -fx-font-weight: bold;");

        vBox.getChildren().add(text);

        return vBox;
    }

    private Slider creatSlider(int min, int max, int value) {
        Slider slider = new Slider(min, max, value);
        int trackHeight = 5;
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(1);
        String trackStyle = "-fx-control-inner-background: #3aa198; -fx-background-insets: " + (trackHeight / 2) + " 0 "
                + (trackHeight / 2) + " 0;";
        slider.setStyle(trackStyle);
        slider.setStyle("-fx-tick-label-fill: #e74c3c;");
        slider.setStyle("-fx-font-size: 10px;");
        slider.setPrefHeight(5);
        slider.cursorProperty().set(Cursor.HAND);

        return slider;
    }

    private Button createReseButton(String string) {
        Button button = new Button();
        button.setText(string);
        button.setPrefSize(180, 40);
        button.cursorProperty().set(Cursor.HAND);
        button.styleProperty().set(
                "-fx-background-color: #3aa198; -fx-border-width: 3px; -fx-border-color: #fdc88e; -fx-border-radius: 2px; -fx-background-radius: 2px; -fx-padding: 2px;  -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: bold;");

        button.onMouseEnteredProperty().set(event -> {
            button.styleProperty().set(
                    "-fx-background-color: #40afa5; -fx-border-width: 3px; -fx-border-color: #fdc88e; -fx-border-radius: 2px; -fx-background-radius: 2px; -fx-padding: 2px;  -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: bold;");
        });
        button.onMouseExitedProperty().set(event -> {
            button.styleProperty().set(
                    "-fx-background-color: #3aa198; -fx-border-width: 3px; -fx-border-color: #fdc88e; -fx-border-radius: 2px; -fx-background-radius: 2px; -fx-padding: 2px;  -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: bold;");
        });

        button.onMousePressedProperty().set(event -> {
            button.styleProperty().set(
                    "-fx-background-color: #318176; -fx-border-width: 3px; -fx-border-color: #fdc88e; -fx-border-radius: 2px; -fx-background-radius: 2px; -fx-padding: 2px;  -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: bold;");
        });
        button.onMouseReleasedProperty().set(event -> {
            button.styleProperty().set(
                    "-fx-background-color: #40afa5; -fx-border-width: 3px; -fx-border-color: #fdc88e; -fx-border-radius: 2px; -fx-background-radius: 2px; -fx-padding: 2px;  -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: bold;");
        });

        return button;
    }
}

class BarberShop {
    private Semaphore customers = new Semaphore(0);
    private Semaphore chairs = new Semaphore(TheSleepBarber.CHAIRS);
    private Semaphore mutex = new Semaphore(1);
    private Semaphore barbers = new Semaphore(0);
    private Queue<Integer> waitingCustomers = new LinkedList<>();

    public void barber() throws InterruptedException {
        while (true) {

            mutex.acquire();
            if (waitingCustomers.isEmpty()) {
                System.out.println("Barber is sleeping.");
                mutex.release();
                customers.acquire();
            } else {
                int customerId = waitingCustomers.poll();
                mutex.release();
                barbers.acquire();
                chairs.release();

                System.out.println("Barber is cutting hair for customer " + customerId);
                Thread.sleep(3000);
            }
        }
    }

    public void customer(int id) throws InterruptedException {
        System.out.println("Customer " + id + " is entering the shop.");

        mutex.acquire();
        if (waitingCustomers.size() < TheSleepBarber.CHAIRS) {
            waitingCustomers.offer(id);
            System.out.println("Customer " + id + " is waiting in the waiting room.");
            customers.release();
            mutex.release();
            barbers.release();
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
                int randomDelay = ThreadLocalRandom.current().nextInt(1, 3);
                Thread.sleep(randomDelay * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class CustomerGeneratorPaused extends Thread {
    private BarberShop shop;
    private int customerId = 1;

    public CustomerGeneratorPaused(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                shop.customer(customerId);
                customerId++;
                int randomDelay = ThreadLocalRandom.current().nextInt(1, 5);
                Thread.sleep(randomDelay * 100);

                if (customerId == 20) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Customer generator thread interrupted.");
        }
    }
}
