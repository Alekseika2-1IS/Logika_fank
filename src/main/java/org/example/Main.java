package org.example; // Пакет, в котором живут все классы проекта

import javafx.application.Application; // Базовый класс любого JavaFX-приложения (от него наследуемся)
import javafx.geometry.Insets; // Класс внутренних отступов контейнера (padding)
import javafx.scene.Scene; // "Сцена" — корневой контейнер всех элементов формы
import javafx.scene.control.Button; // Класс кнопки
import javafx.scene.control.Label; // Класс текстовой надписи (метки)
import javafx.scene.control.TextField; // Класс однострочного поля ввода текста
import javafx.scene.layout.GridPane; // Контейнер-сетка: элементы расставляются по строкам и столбцам
import javafx.stage.Stage; // "Сцена-театр": само окно приложения

public class Main extends Application { // Главный класс приложения, наследник JavaFX Application

    private final TextField arg1Field = new TextField(); // Поле ввода первого числа (Аргумент 1)
    private final TextField arg2Field = new TextField(); // Поле ввода второго числа (Аргумент 2)
    private final Label resultLabel = new Label("Результат: —"); // Метка, в которую выводим результат вычислений

    @Override // Пометка: метод переопределяет метод родительского класса Application
    public void start(Stage stage) { // Метод вызывается JavaFX при старте: сюда приходит наше окно
        GridPane grid = new GridPane(); // Создаём сетку-контейнер для элементов формы
        grid.setPadding(new Insets(20)); // Отступ 20 px от краёв окна до содержимого сетки
        grid.setHgap(10); // Горизонтальный зазор 10 px между столбцами сетки
        grid.setVgap(10); // Вертикальный зазор 10 px между строками сетки

        grid.add(new Label("Аргумент 1:"), 0, 0); // Надпись "Аргумент 1:" в столбец 0, строку 0
        grid.add(arg1Field, 1, 0); // Поле ввода первого числа в столбец 1, строку 0
        grid.add(new Label("Аргумент 2:"), 0, 1); // Надпись "Аргумент 2:" в столбец 0, строку 1
        grid.add(arg2Field, 1, 1); // Поле ввода второго числа в столбец 1, строку 1

        Button calcButton = new Button("Вычислить"); // Кнопка "Вычислить"
        calcButton.setOnAction(e -> calculate()); // Обработчик клика: вызвать метод calculate()
        grid.add(calcButton, 0, 2, 2, 1); // Кнопка в столбец 0, строку 2, растянуть на 2 столбца, 1 строку

        grid.add(resultLabel, 0, 3, 2, 1); // Метка результата в столбец 0, строку 3, растянуть на 2 столбца

        Button namesButton = new Button("Форма списка имён"); // Кнопка перехода ко второй форме (задание 2)
        namesButton.setOnAction(e -> NamesForm.open()); // По клику открыть окно списка имён (NamesForm.open())
        grid.add(namesButton, 0, 4, 2, 1); // Кнопка в столбец 0, строку 4, растянуть на 2 столбца

        Scene scene = new Scene(grid, 380, 300); // Создаём сцену 380x300 px с нашей сеткой внутри
        stage.setTitle("Демоэкзамен: ввод/вывод"); // Заголовок окна
        stage.setScene(scene); // Помещаем сцену в окно
        stage.show(); // Показываем окно на экране
    }

    private void calculate() { // Метод вычисления (вызывается по клику на "Вычислить")
        try { // Блок try: пробуем выполнить код, который может бросить исключение
            double a = Double.parseDouble(arg1Field.getText().trim()); // Текст поля 1 → убрать пробелы → преобразовать в число
            double b = Double.parseDouble(arg2Field.getText().trim()); // Текст поля 2 → убрать пробелы → преобразовать в число
            resultLabel.setText("Результат:\n" // Выводим в метку заголовок и три строки (\n — перенос строки)
                    + fmt(a) + " + " + fmt(b) + " = " + fmt(a + b) + "\n" // Строка 1: сумма a и b
                    + fmt(a) + " - " + fmt(b) + " = " + fmt(a - b) + "\n" // Строка 2: разность a и b
                    + fmt(a) + " * " + fmt(b) + " = " + fmt(a * b)); // Строка 3: произведение a и b
        } catch (NumberFormatException ex) { // Ловим исключение: текст не удалось превратить в число
            resultLabel.setText("Внимание: введите оба аргумента числами!"); // Показываем предупреждение вместо результата
        }
    }

    private String fmt(double d) { // Вспомогательный метод красивого форматирования числа
        if (d == Math.rint(d) && !Double.isInfinite(d)) { // Проверка: число целое (без дробной части) и не бесконечность
            return String.valueOf((long) d); // Целое → возвращаем без хвоста ".0" (приведение к long)
        }
        return String.valueOf(d); // Иначе возвращаем как есть (например 2.5)
    }

    public static void main(String[] args) { // Точка входа программы: отсюда начинает работу JVM
        launch(args); // Запуск JavaFX-машины: создаст окно и вызовет метод start()
    }
}