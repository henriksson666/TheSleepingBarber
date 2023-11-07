import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.application.Platform;
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

public class TheSleepingBarber extends Application {
    public static final int CHAIRS = 5;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean[] isReset = { false };
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
        Button resetButton = createResetButton("Play Animation");
        Button togglePlayPauseBarber = createTogglePlayPauseButton("Play/Pause");
        Button togglePlayPauseCustomer = createTogglePlayPauseButton("Play/Pause");
        VBox barberControlVBox = createControlVBox("Barber");
        VBox customerControlVBox = createControlVBox("Customer");
        VBox informationControlVBox = createInformationControlVBox();
        Slider barberSlider = creatSlider(1, 10, 2);
        Slider customerSlider = creatSlider(1, 10, 2);
        Text barberSliderLabel = createText("Barber Speed: ");
        Text barberSpeed = new Text("" + (int) barberSlider.getValue());
        Text customerSliderLabel = createText("Customer Speed: ");
        Text customerSpeed = new Text("" + (int) customerSlider.getValue());
        Text waitingRoomCustomersLabel = createText("Waiting Room Customers: ");
        Text waitingRoomCustomers = new Text("0");
        Text barberStatusLabel = createText("Barber Status: ");
        Text barberStatus = new Text("Inactive");
        Text customerStatusLabel = createText("Customer Status: ");
        Text customerStatus = new Text("Inactive");
        Text servedCustomersLabel = createText("Served Customers: ");
        Text servedCustomers = new Text("0");
        Text lostCustomersLabel = createText("Lost Customers: ");
        Text lostCustomers = new Text("0");
        informationControlVBox.getChildren().addAll(barberSliderLabel, barberSpeed, customerSliderLabel, customerSpeed,
                waitingRoomCustomersLabel, waitingRoomCustomers, barberStatusLabel, barberStatus, customerStatusLabel,
                customerStatus, servedCustomersLabel, servedCustomers, lostCustomersLabel, lostCustomers);

        barberControlVBox.getChildren().addAll(barberSlider, togglePlayPauseBarber);
        customerControlVBox.getChildren().addAll(customerSlider, togglePlayPauseCustomer);
        permanentControlVBox.getChildren().addAll(resetButton, barberControlVBox, customerControlVBox);
        root.getChildren().add(informationControlVBox);

        BarberShop barberShop = new BarberShop(waitingRoomCustomers, servedCustomers, lostCustomers);
        Barber[] barberThread = { new Barber(barberShop) };
        CustomerGenerator[] customerGeneratorThread = { new CustomerGenerator(barberShop) };

        resetButton.setOnAction(event -> {
            resetButton.setText("Reset");
            togglePlayPauseBarber.setText("Pause");
            togglePlayPauseCustomer.setText("Pause");
            barberStatus.setText("Active");
            customerStatus.setText("Active");
            togglePlayPauseBarber.setDisable(false);
            togglePlayPauseCustomer.setDisable(false);

            if (isReset[0]) {
                barberShop.reset();
                barberSlider.setValue(2);
                customerSlider.setValue(2);
                barberThread[0].interrupt();
                customerGeneratorThread[0].interrupt();
                barberThread[0] = new Barber(barberShop);
                customerGeneratorThread[0] = new CustomerGenerator(barberShop);
                barberShop.resumeThread();
                customerGeneratorThread[0].resumeThread();
            }
            barberShop.setBarberSpeed((int) barberSlider.getValue());
            customerGeneratorThread[0].setCustomerSpeed((int) customerSlider.getValue());
            barberThread[0].start();
            customerGeneratorThread[0].start();
            isReset[0] = true;
        });

        togglePlayPauseBarber.setOnAction(event -> {
            if (togglePlayPauseBarber.getText().equals("Pause")) {
                togglePlayPauseBarber.setText("Play");
                barberStatus.setText("Inactive");
                barberShop.pauseThread();
            } else {
                togglePlayPauseBarber.setText("Pause");
                barberStatus.setText("Active");
                barberShop.resumeThread();
            }
        });

        togglePlayPauseCustomer.setOnAction(event -> {
            if (togglePlayPauseCustomer.getText().equals("Pause")) {
                togglePlayPauseCustomer.setText("Play");
                customerStatus.setText("Inactive");
                customerGeneratorThread[0].pauseThread();
            } else {
                togglePlayPauseCustomer.setText("Pause");
                customerStatus.setText("Active");
                customerGeneratorThread[0].resumeThread();
            }
        });

        togglePlayPauseBarber.setDisable(true);
        togglePlayPauseCustomer.setDisable(true);

        barberSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            barberSpeed.setText("" + newValue.intValue());
            barberShop.setBarberSpeed(newValue.intValue());
        });

        customerSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            customerSpeed.setText("" + newValue.intValue());
            customerGeneratorThread[0].setCustomerSpeed(newValue.intValue());
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
        // int xProperty = 500;
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
        // int xProperty = 500;
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
        vBox.setPrefSize(200, 110);
        vBox.setStyle(
                "-fx-background-color: rgba(245, 245, 220, 0.7); -fx-background-radius: 2px; -fx-alignment: center; -fx-padding: 2px;");

        Text text = new Text(string);
        text.setStyle("-fx-fill: #000; -fx-font-size: 15px; -fx-font-weight: bold;");
        vBox.setSpacing(7);

        vBox.getChildren().add(text);

        return vBox;
    }

    private Slider creatSlider(int min, int max, int value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(false);
        slider.setMajorTickUnit(1);
        slider.setStyle(
                "-fx-base: #5e9cff; -fx-track: green; -fx-control-inner-background: #041736; -fx-control-outer-background: #041736;");

        slider.cursorProperty().set(Cursor.HAND);

        return slider;
    }

    private Button createResetButton(String string) {
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

    private Button createTogglePlayPauseButton(String string) {
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
    private Semaphore chairs = new Semaphore(TheSleepingBarber.CHAIRS);
    private Semaphore mutex = new Semaphore(1);
    private Semaphore barbers = new Semaphore(0);
    private Queue<Integer> waitingCustomers = new LinkedList<>();
    private int servedCustomersCount = 1;
    private int lostCustomersCount = 1;
    private volatile int barberSpeed;
    private Text waitingRoomCustomers;
    private Text servedCustomers;
    private Text lostCustomers;
    private volatile boolean isRunning = true;

    public BarberShop(Text waitingRoomCustomers, Text servedCustomers, Text lostCustomers) {
        this.waitingRoomCustomers = waitingRoomCustomers;
        this.servedCustomers = servedCustomers;
        this.lostCustomers = lostCustomers;
    }

    public void barber() throws InterruptedException {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (isRunning) {

                    mutex.acquire();
                    if (waitingCustomers.isEmpty()) {
                        System.out.println("Barber is sleeping.");
                        mutex.release();
                        customers.acquire();
                    } else {
                        int customerId = waitingCustomers.poll();
                        waitingRoomCustomers.setText("" + waitingCustomers.size());
                        servedCustomers.setText("" + servedCustomersCount++);
                        mutex.release();
                        barbers.acquire();
                        chairs.release();

                        System.out.println("Barber is cutting hair for customer " + customerId);
                        Thread.sleep(barberSpeed);

                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Barber thread interrupted.");
        }
    }

    public void customer(int id) throws InterruptedException {
        System.out.println("Customer " + id + " is entering the shop.");

        mutex.acquire();
        if (waitingCustomers.size() < TheSleepingBarber.CHAIRS) {
            waitingCustomers.offer(id);
            waitingRoomCustomers.setText("" + waitingCustomers.size());
            System.out.println("Customer " + id + " is waiting in the waiting room.");
            customers.release();
            mutex.release();
            barbers.release();
        } else {
            System.out.println("Customer " + id + " is leaving because the shop is full.");
            lostCustomers.setText("" + lostCustomersCount++);
            mutex.release();
        }
    }

    public void pauseThread() {
        isRunning = false;
    }

    public void resumeThread() {
        isRunning = true;
    }

    public void setBarberSpeed(int barberSpeed) {
        this.barberSpeed = barberSpeed * 1000;
    }

    public void updateWaitingRoomCustomersText(int value) {
        Platform.runLater(() -> waitingRoomCustomers.setText(String.valueOf(value)));
    }

    public void updateServedCustomersText(int value) {
        Platform.runLater(() -> servedCustomers.setText(String.valueOf(value)));
    }

    public void updateLostCustomersText(int value) {
        Platform.runLater(() -> lostCustomers.setText(String.valueOf(value)));
    }

    public void reset() {
        servedCustomersCount = 1;
        lostCustomersCount = 1;
        waitingCustomers.clear();

        Platform.runLater(() -> {
            waitingRoomCustomers.setText("0");
            servedCustomers.setText("0");
            lostCustomers.setText("0");
        });
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
    private volatile boolean isRunning = true;
    private volatile int customerSpeed;

    public CustomerGenerator(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (isRunning) {

                    shop.customer(customerId);
                    customerId++;
                    Thread.sleep(customerSpeed);

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Customer generator thread interrupted.");
        }
    }

    public void pauseThread() {
        isRunning = false;
    }

    public void resumeThread() {
        isRunning = true;
    }

    public void setCustomerSpeed(int customerSpeed) {
        this.customerSpeed = customerSpeed * 1000;
    }
}

class RandomCustomerGenerator extends Thread {
    private BarberShop shop;
    private int customerId = 1;
    private volatile boolean isRunning = true;
    private volatile int customerSpeed = 1;

    public RandomCustomerGenerator(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if(isRunning){
                    shop.customer(customerId);
                    customerId++;
                    int randomDelay = ThreadLocalRandom.current().nextInt(1, customerSpeed);
                    Thread.sleep(randomDelay * 100);

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }

            }
        } catch (InterruptedException e) {
            System.out.println("Customer generator thread interrupted.");
        }
    }

    public void pauseThread() {
        isRunning = false;
    }

    public void resumeThread() {
        isRunning = true;
    }

    public void setCustomerSpeed(int customerSpeed) {
        this.customerSpeed = customerSpeed * 1000;
    }
}
