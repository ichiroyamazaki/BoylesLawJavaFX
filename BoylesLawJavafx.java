
package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoylesLaw2 extends Application {

    private static final double PARTICLE_RADIUS = 5;
    private static final double MIN_TEMPERATURE = 0.5;
    private static final double MAX_TEMPERATURE = 100.0;
    private static final double DEFAULT_CHAMBER_WIDTH = 800;
    private static final double DEFAULT_CHAMBER_HEIGHT = 400;

    private int particleCount = 100;
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private double temperature = MIN_TEMPERATURE;
    private Slider temperatureSlider;
    private Group root;
    private Color particleColor = Color.BLUE;

    private double chamberWidth = DEFAULT_CHAMBER_WIDTH;
    private double chamberHeight = DEFAULT_CHAMBER_HEIGHT;

    private AnimationTimer timer;
    private Labeled particleCountLabel;
    private double volume1 = 1.0;
    private double volume2 = 1.0;
    private Label volume1Label;
    private Label volume2Label;

    @Override
    public void start(Stage primaryStage) {
        root = new Group();
        Scene scene = new Scene(root, DEFAULT_CHAMBER_WIDTH, DEFAULT_CHAMBER_HEIGHT);

        // Create chamber
        Rectangle chamber = new Rectangle(0, 0, chamberWidth, chamberHeight);
        chamber.setFill(Color.TRANSPARENT);
        chamber.setStroke(Color.BLACK);
        root.getChildren().add(chamber);

        // Create particles
        initializeParticles();

        // Temperature adjustment slider
        temperatureSlider = new Slider(MIN_TEMPERATURE, MAX_TEMPERATURE, MIN_TEMPERATURE);
        temperatureSlider.setShowTickMarks(true);
        temperatureSlider.setShowTickLabels(true);
        temperatureSlider.setBlockIncrement(0.1);
        temperatureSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            temperature = newValue.doubleValue();
            particles.forEach(particle -> particle.adjustSpeed(temperature));
            calculatePressure();
        });

        // Slider value label
        Label temperatureLabel = new Label("Temperature: " + MIN_TEMPERATURE);
        temperatureSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                temperatureLabel.setText("Temperature: " + String.format("%.1f", newValue)));

        // Color picker for particle color selection
        ColorPicker colorPicker = new ColorPicker(particleColor);
        colorPicker.setOnAction(event -> {
            particleColor = colorPicker.getValue();
            particles.forEach(particle -> particle.setColor(particleColor));
        });

        // Adjustable chamber width slider
        Slider widthSlider = new Slider(100, 1350, DEFAULT_CHAMBER_WIDTH);
        widthSlider.setShowTickMarks(true);
        widthSlider.setShowTickLabels(true);
        widthSlider.setBlockIncrement(10);
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            chamberWidth = newValue.doubleValue();
            chamber.setWidth(chamberWidth);
            resetSimulation();
        });

        // Adjustable chamber height slider
        Slider heightSlider = new Slider(100, 950, DEFAULT_CHAMBER_HEIGHT);
        heightSlider.setShowTickMarks(true);
        heightSlider.setShowTickLabels(true);
        heightSlider.setBlockIncrement(10);
        heightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            chamberHeight = newValue.doubleValue();
            chamber.setHeight(chamberHeight);
            resetSimulation();
        });

        // Volume adjustment slider 1
        Slider volumeSlider1 = new Slider(0.1, 5.0, volume1);
        volumeSlider1.setShowTickMarks(true);
        volumeSlider1.setShowTickLabels(true);
        volumeSlider1.setBlockIncrement(0.1);
        volumeSlider1.valueProperty().addListener((observable, oldValue, newValue) -> {
            volume1 = newValue.doubleValue();
            calculatePressure();
        });

        // Volume adjustment slider 2
        Slider volumeSlider2 = new Slider(0.1, 5.0, volume2);
        volumeSlider2.setShowTickMarks(true);
        volumeSlider2.setShowTickLabels(true);
        volumeSlider2.setBlockIncrement(0.1);
        volumeSlider2.valueProperty().addListener((observable, oldValue, newValue) -> {
            volume2 = newValue.doubleValue();
            calculatePressure();
        });

        // Slider value labels
        volume1Label = new Label("Volume 1: " + volume1);
        volume2Label = new Label("Volume 2: " + volume2);

        VBox controlBox = new VBox(10);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.getChildren().addAll(
                new Label("Temperature:"),
                temperatureSlider,
                temperatureLabel,
                new Label("Particle Color:"),
                colorPicker,
                new Label("Chamber Width:"),
                widthSlider,
                new Label("Chamber Height:"),
                heightSlider,
                new Label("Volume 1:"),
                volumeSlider1,
                volume1Label,
                new Label("Volume 2:"),
                volumeSlider2,
                volume2Label
        );
        controlBox.setTranslateX(1650);
        controlBox.setTranslateY(10);
        root.getChildren().add(controlBox);

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(event -> resetSimulation());
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(resetButton);
        controlBox.getChildren().add(buttonBox);

        // Start button
        Button startButton = new Button("Start");
        startButton.setOnAction(event -> startSimulation());

        // Stop button
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(event -> stopSimulation());
        HBox actionButtonBox = new HBox(10);
        actionButtonBox.setAlignment(Pos.CENTER);
        actionButtonBox.getChildren().addAll(startButton, stopButton);
        controlBox.getChildren().add(actionButtonBox);

        // Animation
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                particles.forEach(Particle::move);
            }
        };

        primaryStage.setTitle("Boyle's Law Gas Properties - Moving Particles");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true); // Enable window resizing
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initializeParticles() {
        particles.forEach(particle -> root.getChildren().remove(particle)); // Remove existing particles from the root group
        particles.clear();

        for (int i = 0; i < particleCount; i++) {
            Particle particle = createParticle();
            particles.add(particle);
            root.getChildren().add(particle); // Add the newly created particles to the root group
        }
    }

    @SuppressWarnings("unused")
private void adjustParticleCount(int count) {
        if (count < particleCount) {
            int difference = particleCount - count;
            removeParticles(difference);
        } else if (count > particleCount) {
            int difference = count - particleCount;
            addParticles(difference);
        }
        particleCount = count;
        particleCountLabel.setText("Particle Count: " + particleCount);
    }

    private void removeParticles(int count) {
        if (count >= particles.size()) {
            particles.forEach(particle -> root.getChildren().remove(particle));
            particles.clear();
        } else {
            for (int i = 0; i < count; i++) {
                Particle particle = particles.remove(particles.size() - 1);
                root.getChildren().remove(particle);
            }
        }
    }

    private void addParticles(int count) {
        for (int i = 0; i < count; i++) {
            Particle particle = createParticle();
            particles.add(particle);
            root.getChildren().add(particle);
        }
    }

    private Particle createParticle() {
        Particle particle = new Particle(PARTICLE_RADIUS, particleColor);
        particle.setTranslateX(random.nextDouble() * (chamberWidth - 2 * PARTICLE_RADIUS) + PARTICLE_RADIUS);
        particle.setTranslateY(random.nextDouble() * (chamberHeight - 2 * PARTICLE_RADIUS) + PARTICLE_RADIUS);
        return particle;
    }

    private void resetSimulation() {
        resetTemperature();
        initializeParticles();
        temperatureSlider.setValue(temperature);
        calculatePressure();
    }

    private void resetTemperature() {
        temperature = MIN_TEMPERATURE;
        particles.forEach(particle -> particle.adjustSpeed(temperature));
    }

    private void calculatePressure() {
        new DecimalFormat("#.00");
        volume1Label.setText("Volume 1: " + volume1);
        volume2Label.setText("Volume 2: " + volume2);
    }

    private void startSimulation() {
        timer.start();
    }

    private void stopSimulation() {
        timer.stop();
    }

    private class Particle extends Circle {
        private double dx;
        private double dy;

        public Particle(double radius, Color color) {
            super(radius);
            setColor(color);
            dx = random.nextDouble() * 4 - 2;
            dy = random.nextDouble() * 4 - 2;
        }

        public void move() {
            double newX = getTranslateX() + dx;
            double newY = getTranslateY() + dy;

            if (newX < getRadius() || newX > chamberWidth - getRadius()) {
                dx = -dx;
            }
            if (newY < getRadius() || newY > chamberHeight - getRadius()) {
                dy = -dy;
            }

            setTranslateX(newX);
            setTranslateY(newY);
        }

        public void adjustSpeed(double temperature) {
            double speedFactor = temperature / MIN_TEMPERATURE;
            dx *= speedFactor;
            dy *= speedFactor;
        }

        public void setColor(Color color) {
            setFill(color);
        }
    }
}
