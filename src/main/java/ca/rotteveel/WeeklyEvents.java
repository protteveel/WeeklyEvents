package ca.rotteveel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.format.TextStyle;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class WeeklyEvents extends Application {

    private Map<String, Map<String, List<String>>> schedule;
    private Stage primaryStage;
    private Timer timer;

    private final String schedulePath = "./WeeklyEvents.json";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Weekly Events");

        // Load the schedule from the JSON file
        loadSchedule();

        // Create buttons for the main screen
        Button editButton = new Button("Edit");
        Button runButton = new Button("Run");
        Button quitButton = new Button("Quit");

        editButton.setOnAction(e -> enterEditMode());
        runButton.setOnAction(e -> enterRunMode());
        quitButton.setOnAction(e -> {
            if (timer != null) {
                timer.cancel();
            }
            Platform.exit();
        });

        // Layout for the main screen
        HBox buttonLayout = new HBox(10);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(editButton, runButton, quitButton);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(buttonLayout);

        Scene mainScene = new Scene(mainLayout, 400, 300);
        primaryStage.setScene(mainScene);
        primaryStage.show();
        primaryStage.requestFocus();
    }

    public static String concatenateDetails(String[] details) {
        if (details == null || details.length == 0) {
            return "";
        }

        StringBuilder concatenatedString = new StringBuilder();

        for (int i = 0; i < details.length; i++) {
            concatenatedString.append(details[i]);
            if (i != details.length - 1) {
                concatenatedString.append("\n");
            }
        }

        return concatenatedString.toString();
    }

    private void enterEditMode() {
        Stage editStage = new Stage();
        editStage.setTitle("Edit");

        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        // Create the headers
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-background-color: darkgrey; -fx-text-fill: white; -fx-font-weight: bold;");
            dayLabel.setMinWidth(100); // Adjust width as needed
            dayLabel.setAlignment(Pos.CENTER); // Center the text
            gridPane.add(dayLabel, i + 1, 0);
        }

        for (int i = 0; i < 48; i++) {
            Label timeLabel = new Label(String.format("%02d:%02d", 7 + (i / 4), (i % 4) * 15));
            timeLabel.setStyle("-fx-background-color: darkgrey; -fx-text-fill: white; -fx-font-weight: bold;");
            timeLabel.setMinWidth(60); // Set minimum width to ensure the full time is displayed
            timeLabel.setMaxWidth(60); // Set maximum width to ensure consistent column width
            timeLabel.setAlignment(Pos.CENTER); // Center the text
            gridPane.add(timeLabel, 0, i + 1);
        }

        // Create the cells
        TextArea[][] textAreas = new TextArea[48][7];
        for (int row = 0; row < 48; row++) {
            for (int col = 0; col < 7; col++) {
                TextArea textArea = new TextArea();
                textArea.setWrapText(true);
                textArea.setPrefRowCount(3);
                textAreas[row][col] = textArea;
                gridPane.add(textArea, col + 1, row + 1);
            }
        }

        // Load existing data into the text areas
        for (int row = 0; row < 48; row++) {
            for (int col = 0; col < 7; col++) {
                String day = daysOfWeek[col];
                String time = String.format("%02d:%02d", 7 + (row / 4), (row % 4) * 15);
                if (schedule.containsKey(day) && schedule.get(day).containsKey(time)) {
                    textAreas[row][col].setText(String.join("\n", schedule.get(day).get(time)));
                }
            }
        }

        // Create Save and Cancel buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            saveSchedule(textAreas, daysOfWeek);
            editStage.close();
        });

        cancelButton.setOnAction(e -> editStage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(gridPane, buttonBox);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene editScene = new Scene(scrollPane, 800, 600);
        editStage.setScene(editScene);
        editStage.show();
    }

    private void enterRunMode() {
        Stage runStage = new Stage();
        runStage.setTitle("Run");

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            if (timer != null) {
                timer.cancel();
            }
            runStage.close();
        });

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(cancelButton);

        Scene runScene = new Scene(vbox, 400, 300);
        runStage.setScene(runScene);
        runStage.show();

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> checkForMessages());
            }
        }, 0, 60000); // Check every minute
    }

    private boolean isMessagesEmpty(List<String> messages) {
        // Check if the list itself is null or empty
        if (messages == null || messages.isEmpty()) {
            return true;
        }
        
        // Check if all strings in the list are null or empty
        for (String message : messages) {
            if (message != null && !message.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    private void checkForMessages() {
        // Get the current day of the week and time
        String currentDay = java.time.LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String currentTime = java.time.LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES).toString();

        if (schedule.containsKey(currentDay) && schedule.get(currentDay).containsKey(currentTime)) {
            List<String> messages = schedule.get(currentDay).get(currentTime);
            if (!isMessagesEmpty(messages)) {

                // Play the sound
                String soundFilePath = new File("events.mp3").toURI().toString();
                Media sound = new Media(soundFilePath);
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.play();

                // Create the message window
                Stage messageStage = new Stage();
                messageStage.setTitle("It's time!");
                VBox messageBox = new VBox(10);
                for (String message : messages) {
                    messageBox.getChildren().add(new Label(message));
                }
                Button okButton = new Button("OK");
                okButton.setOnAction(e -> messageStage.close());
                messageBox.getChildren().add(okButton);
                messageBox.setAlignment(Pos.CENTER);
                Scene messageScene = new Scene(messageBox, 300, 200);
                messageStage.setScene(messageScene);
                messageStage.show();
            }
        }
    }

    private void playSound(String soundFileName) {
        try {
            File soundFile = new File(soundFileName);
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(soundFile));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSchedule() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Map<String, Map<String, List<String>>>> typeRef = new TypeReference<Map<String, Map<String, List<String>>>>() {};
            schedule = objectMapper.readValue(new File("WeeklyEvents.json"), typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSchedule(TextArea[][] textAreas, String[] daysOfWeek) {
        for (int row = 0; row < 48; row++) {
            for (int col = 0; col < 7; col++) {
                String day = daysOfWeek[col];
                String time = String.format("%02d:%02d", 7 + (row / 4), (row % 4) * 15);
                String[] messages = textAreas[row][col].getText().split("\n");
                if (!schedule.containsKey(day)) {
                    schedule.put(day, new HashMap<>());
                }
                schedule.get(day).put(time, Arrays.asList(messages));
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("WeeklyEvents.json"), schedule);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
