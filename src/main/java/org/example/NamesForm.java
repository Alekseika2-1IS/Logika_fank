package org.example;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;

public class NamesForm {

    private final ListView<String> namesList = new ListView<>();
    private final TextField nameField = new TextField();
    private final Deque<String> addedHistory = new ArrayDeque<>(); // для Ctrl+Z

    public static void open() {
        NamesForm form = new NamesForm();
        Stage stage = new Stage();
        stage.setTitle("Демоэкзамен: список имён");
        stage.setScene(form.buildScene());
        stage.show();
    }

    private Scene buildScene() {
        namesList.getItems().addAll("Алексей", "Андрей", "Варвара");
        namesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        namesList.setCellFactory(lv -> new NameCell());
        // 2.b.ii: после клика список в фокусе — стрелки вверх/вниз ходят по списку
        namesList.setOnMouseClicked(e -> namesList.requestFocus());

        Label title = new Label("Список имён:");
        Label prompt = new Label("Введите имя:");

        Button addButton = new Button("Добавить в список");
        addButton.setOnAction(e -> addName());

        Button deleteButton = new Button();
        deleteButton.setGraphic(new Label("🗑")); // 2.b: кнопка с изображением
        deleteButton.setTooltip(new Tooltip("Удалить выбранный элемент"));
        deleteButton.setOnAction(e -> deleteSelected());

        Button checkButton = new Button("Проверить на символы");
        checkButton.setOnAction(e -> checkBadSymbols());

        VBox box = new VBox(10, title, namesList, prompt, nameField,
                addButton, deleteButton, checkButton);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box, 360, 430);
        // 2.b.iii: отмена последней вставки по Ctrl+Z
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN).match(e)) {
                undoLastAdd();
                e.consume();
            }
        });
        return scene;
    }

    // 2.c: нормализация данных
    private String normalize(String raw) {
        String noSpaces = raw.replaceAll("\\s+", ""); // 2.c.i: пробелы до/после/внутри
        if (noSpaces.isEmpty()) return noSpaces;
        // 2.c.ii / 2.c.iii / 2.c.iv: первая заглавная, остальные строчные
        return Character.toUpperCase(noSpaces.charAt(0))
                + noSpaces.substring(1).toLowerCase();
    }

    private void addName() {
        String name = normalize(nameField.getText());
        if (name.isEmpty()) return;
        // 2.e: проверка дубликатов
        if (namesList.getItems().contains(name)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Уже есть в списке", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return; // 2.e.ii: вставка НЕ осуществляется
        }
        namesList.getItems().add(name); // 2.a: добавление только кнопкой
        addedHistory.push(name);
        nameField.clear();
    }

    private void deleteSelected() {
        String selected = namesList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        namesList.getItems().remove(selected);
    }

    private void undoLastAdd() {
        String last = addedHistory.poll();
        if (last != null) namesList.getItems().remove(last);
    }

    // 2.d: поиск невалидных символов
    private void checkBadSymbols() {
        String text = nameField.getText();
        if (text == null || text.isEmpty()) return;
        boolean hasBad = text.chars().anyMatch(ch ->
                !Character.isLetter(ch) && !Character.isWhitespace(ch));
        if (!hasBad) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Внимание: Есть не текстовые символы, очистить?",
                new ButtonType("Очистить"), new ButtonType("Оставить"));
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(type -> {
            if ("Очистить".equals(type.getText())) {
                nameField.setText(text.replaceAll("[^\\p{L}\\s]", ""));
            }
        });
    }

    // 2.b.i: подсветка выбранного элемента иным цветом
    private static class NameCell extends ListCell<String> {
        NameCell() {
            selectedProperty().addListener((o, old, sel) -> applyStyle());
        }
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item);
            applyStyle();
        }
        private void applyStyle() {
            setStyle(!isEmpty() && isSelected()
                    ? "-fx-background-color: #ffe08a;" : "");
        }
    }
}