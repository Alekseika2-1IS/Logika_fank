package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    private final TextField arg1Field = new TextField();
    private final TextField arg2Field = new TextField();
    private final Label resultLabel = new Label("Результат: —");

    @Override
    public void start(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Аргумент 1:"), 0, 0);
        grid.add(arg1Field, 1, 0);
        grid.add(new Label("Аргумент 2:"), 0, 1);
        grid.add(arg2Field, 1, 1);

        Button calcButton = new Button("Вычислить");
        calcButton.setOnAction(e -> calculate());
        grid.add(calcButton, 0, 2, 2, 1);

        grid.add(resultLabel, 0, 3, 2, 1);

        Scene scene = new Scene(grid, 380, 260);
        stage.setTitle("Демоэкзамен: ввод/вывод");
        stage.setScene(scene);
        stage.show();
    }

    private void calculate() {
        try {
            double a = Double.parseDouble(arg1Field.getText().trim());
            double b = Double.parseDouble(arg2Field.getText().trim());
            resultLabel.setText("Результат:\n"
                    + fmt(a) + " + " + fmt(b) + " = " + fmt(a + b) + "\n"
                    + fmt(a) + " - " + fmt(b) + " = " + fmt(a - b) + "\n"
                    + fmt(a) + " * " + fmt(b) + " = " + fmt(a * b));
        } catch (NumberFormatException ex) {
            resultLabel.setText("Внимание: введите оба аргумента числами!");
        }
    }

    private String fmt(double d) {
        if (d == Math.rint(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    public static void main(String[] args) {
        launch(args);
    }
}