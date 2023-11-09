import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TheSleepingBarber extends Application {
    public static final int CHAIRS = 5;
    public static Pane root;

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean[] isReset = { false };
        boolean[] isRandom = { false };
        root = createPane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("The Sleeping Barber");
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
        HBox randomChoice = new HBox();
        randomChoice.setSpacing(5);
        randomChoice.setAlignment(Pos.CENTER);
        CheckBox randomCheckBox = new CheckBox("Random");
        TextField randomTextField = new TextField();
        randomTextField.setPrefSize(50, 20);
        Button randomButton = new Button("Apply");
        randomChoice.getChildren().addAll(randomCheckBox, randomTextField, randomButton);
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
        permanentControlVBox.getChildren().addAll(resetButton, barberControlVBox, customerControlVBox, randomChoice);

        ImageView barberImageView = createBarberView();
        //ImageView customerImageView = createCustomerImageView();
        root.getChildren().addAll(informationControlVBox, barberImageView);

        BarberShop barberShop = new BarberShop(waitingRoomCustomers, servedCustomers, lostCustomers);
        Barber[] barberThread = { new Barber(barberShop) };
        CustomerGenerator[] customerGeneratorThread = { new CustomerGenerator(barberShop) };

        randomTextField.setDisable(true);
        randomButton.setDisable(true);

        randomCheckBox.setOnAction(event -> {
            if (randomCheckBox.isSelected()) {
                isRandom[0] = true;
                customerSlider.setDisable(true);
                randomTextField.setDisable(false);
                randomButton.setDisable(false);
                customerGeneratorThread[0].setRandom(true);
            } else {
                isRandom[0] = false;
                customerSlider.setDisable(false);
                randomButton.setDisable(true);
                randomTextField.setDisable(true);
                customerGeneratorThread[0].setCustomerSpeed((int) customerSlider.getValue());
                customerGeneratorThread[0].setRandom(false);
            }
        });

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

            if (!isRandom[0]) {
                customerGeneratorThread[0].setRandom(false);
                customerGeneratorThread[0].setCustomerSpeed((int) customerSlider.getValue());
            } else {
                customerGeneratorThread[0].setRandom(true);
                customerGeneratorThread[0].setRandomCustomerSpeed(Integer.parseInt(randomTextField.getText()));
            }

            barberThread[0].start();
            customerGeneratorThread[0].start();
            isReset[0] = true;
        });

        randomButton.setOnAction(event -> {
            if (randomTextField.getText().isEmpty()) {
                randomTextField.setText("2");
            }
            customerGeneratorThread[0].setRandom(true);
            customerGeneratorThread[0].setRandomCustomerSpeed(
                    randomTextField.getText().equals("1") ? Integer.parseInt(randomTextField.getText()) + 1
                            : Integer.parseInt(randomTextField.getText()));
            customerSpeed.setText("Random between 1 and " + (Integer.parseInt(randomTextField.getText()) + 1));
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

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            updateBarberImage(barberImageView);
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
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

    private static void updateBarberImage(ImageView barberImageView) {
        if (BarberShop.isSleeping()) {
            barberImageView.setImage(new Image("sleepingbarber.png"));
            barberImageView.setFitWidth(270);
            barberImageView.setFitHeight(270);
            barberImageView.translateXProperty().set(250);
            barberImageView.translateYProperty().set(389);
        } else {
            barberImageView.setImage(new Image("activebarber.png"));
            barberImageView.setFitWidth(270);
            barberImageView.setFitHeight(270);
            barberImageView.translateXProperty().set(190);
            barberImageView.translateYProperty().set(350);
        }
    }

    /* public void updateCustomer(ImageView customerImageView) {
        customerImageView.setImage(new Image("customer.png"));
        customerImageView.setFitWidth(290);
        customerImageView.setFitHeight(290);
        customerImageView.translateXProperty().set(1000);
        customerImageView.translateYProperty().set(345);

    } */

    public static void animateCustomerEntering(int customerId) {
        ImageView customerImageView = new ImageView(new Image("customer.png"));
        customerImageView.setPreserveRatio(true);
        customerImageView.setFitWidth(290);
        customerImageView.setFitHeight(290);
        customerImageView.setTranslateX(1100);
        customerImageView.setTranslateY(345);
        BarberShop.setWaitingCustomersImageView(customerImageView);
        root.getChildren().add(customerImageView);

        TranslateTransition enteringTransition = new TranslateTransition(Duration.millis(1000), customerImageView);
        enteringTransition.setToX(825);
        enteringTransition.setToY(345);
        enteringTransition.play();
        enteringTransition.setOnFinished(event -> {
            customerImageView.setImage(new Image("customersitting.png"));
            customerImageView.setFitWidth(230);
            customerImageView.setFitHeight(250);
            customerImageView.setPreserveRatio(true);
        });
        //customerImageView.setImage(new Image("customersitting.png"));
    }

    public static void animateCustomerLeaving(ImageView customerImageView) {
        customerImageView.setPreserveRatio(true);
        TranslateTransition leavingTransition = new TranslateTransition(Duration.millis(1000), customerImageView);
        leavingTransition.setToX(1000);
        leavingTransition.setToY(345);
        leavingTransition.setOnFinished(event -> {
            root.getChildren().remove(customerImageView);
        });
        leavingTransition.play();
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
        vBox.translateYProperty().set(250);
        vBox.setPrefSize(200, 400);
        vBox.setStyle(
                "-fx-background-color: radial-gradient(radius 180%, #f99832, #fdc88e, #fdc88e); -fx-padding: 5px; -fx-spacing: 10; -fx-alignment: center;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(-1.0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.BLACK);
        vBox.setEffect(shadow);

        return vBox;
    } // createInformationControlVBox

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

    private ImageView createBarberView() {
        ImageView imageView = new ImageView();
        imageView.setImage(new Image("sleepingbarber.png"));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(250);
        imageView.setFitHeight(250);
        imageView.translateXProperty().set(250);
        imageView.translateYProperty().set(389);

        return imageView;
    }

    /* private ImageView createCustomerImageView() {
        ImageView imageView = new ImageView();
        imageView.setImage(new Image("customer.png"));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(290);
        imageView.setFitHeight(290);
        imageView.translateXProperty().set(1000);
        imageView.translateYProperty().set(345);

        return imageView;
    } */
}

class BarberShop {
    private Semaphore customers = new Semaphore(0);
    private Semaphore chairs = new Semaphore(TheSleepingBarber.CHAIRS);
    private Semaphore mutex = new Semaphore(1);
    private Semaphore barbers = new Semaphore(0);
    private Queue<Integer> waitingCustomers = new LinkedList<>();
    private static List<ImageView> waitingCustomersImageView = new ArrayList<>();
    private int servedCustomersCount = 1;
    private int lostCustomersCount = 1;
    private volatile int barberSpeed;
    private Text waitingRoomCustomers;
    private Text servedCustomers;
    private Text lostCustomers;
    private volatile boolean isRunning = true;
    private static volatile boolean isSleeping = true;

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
                        isSleeping = true;
                        mutex.release();
                        customers.acquire();
                    } else {
                        isSleeping = false;
                        int customerId = waitingCustomers.poll();
                        waitingRoomCustomers.setText("" + waitingCustomers.size());
                        servedCustomers.setText("" + servedCustomersCount++);
                        mutex.release();
                        barbers.acquire();
                        chairs.release();

                        System.out.println("Barber is cutting hair for customer " + customerId);
                        /* Platform.runLater(() -> {
                            TheSleepingBarber.animateCustomerLeaving(getWaitingCustomersImageView());
                        }); */
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
        //TheSleepingBarber.animateCustomerEntering(id);

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

    public static boolean isSleeping() {
        return isSleeping;
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

    public ImageView getWaitingCustomersImageView() {
        return waitingCustomersImageView.remove(0);
    }

    public static void setWaitingCustomersImageView(ImageView customersImageView) {
        waitingCustomersImageView.add(customersImageView);
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
    private volatile boolean isRandom = false;
    private volatile int customerSpeed = 1;
    private volatile int randomCustomerSpeed = 2;

    public CustomerGenerator(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (isRunning) {

                    Platform.runLater(() -> {
                        TheSleepingBarber.animateCustomerEntering(customerId);
                    });

                    shop.customer(customerId);
                    customerId++;

                    if (!isRandom) {
                        System.out.println("Customer generator at continuous speed.");
                        Thread.sleep(customerSpeed);
                    } else {
                        System.out.println("Customer generator at random speed.");
                        int randomDelay = ThreadLocalRandom.current().nextInt(1, randomCustomerSpeed);
                        Thread.sleep(randomDelay * 1000);
                    }

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

    public void setRandom(boolean isRandom) {
        this.isRandom = isRandom;
    }

    public void setCustomerSpeed(int customerSpeed) {
        this.customerSpeed = customerSpeed * 1000;
    }

    public void setRandomCustomerSpeed(int randomCustomerSpeed) {
        this.randomCustomerSpeed = randomCustomerSpeed;
    }
}