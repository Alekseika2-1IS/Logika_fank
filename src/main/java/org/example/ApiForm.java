package org.example;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiForm {

    private static final String BASE_URL = "http://192.168.1.200:4444/TransferSimulator/";

    private final ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
            "fullName", "snils", "inn", "email", "identityCard"));
    private final TextArea resultArea = new TextArea();
    private final Label statusLabel = new Label("Выберите тип данных и нажмите «Получить данные»");

    public static void open() {
        ApiForm form = new ApiForm();
        Stage stage = new Stage();
        stage.setTitle("Демоэкзамен: клиент внешнего API");
        stage.setScene(form.buildScene());
        stage.show();
    }

    private Scene buildScene() {
        typeBox.getSelectionModel().selectFirst();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(6);

        Button getDataButton = new Button("Получить данные");
        getDataButton.setOnAction(e -> loadData());

        VBox box = new VBox(10,
                new Label("Тип данных из API:"), typeBox,
                getDataButton,
                new Label("Ответ сервера:"), resultArea,
                statusLabel);
        box.setPadding(new Insets(20));

        return new Scene(box, 430, 380);
    }

    private void loadData() {
        String method = typeBox.getSelectionModel().getSelectedItem();
        if (method == null) {
            statusLabel.setText("Ошибка: сначала выберите тип данных из списка");
            return;
        }
        statusLabel.setText("Запрос к серверу: " + method + " ...");
        resultArea.clear();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return requestFromApi(method);
            }
        };
        task.setOnSucceeded(e -> {
            resultArea.setText(task.getValue());
            statusLabel.setText("Успех: получены данные " + method);
        });
        task.setOnFailed(e -> {
            resultArea.clear();
            statusLabel.setText("Ошибка: " + task.getException().getMessage());
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private String requestFromApi(String method) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + method))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 500) {
            throw new Exception("500 Internal Server Error — проблема на сервере, обратитесь к главному эксперту");
        }
        if (response.statusCode() != 200) {
            throw new Exception("сервер вернул код " + response.statusCode());
        }
        return extractValue(response.body());
    }

    private String extractValue(String json) throws Exception {
        Matcher matcher = Pattern.compile("\"value\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!matcher.find()) {
            throw new Exception("в ответе нет поля value: " + json);
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }
}