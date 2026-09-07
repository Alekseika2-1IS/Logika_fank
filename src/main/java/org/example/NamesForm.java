package org.example; // Пакет, в котором живёт класс формы списка

import javafx.scene.image.Image; // Класс изображения (загрузка png-файла в память)
import javafx.scene.image.ImageView; // Компонент-отображалка изображения на форме
import javafx.geometry.Insets; // Класс внутренних отступов контейнера (padding)
import javafx.scene.Scene; // "Сцена" — корневой контейнер элементов формы
import javafx.scene.control.*; // Импорт всех элементов управления сразу: Button, Label, ListView, Alert и т.д.
import javafx.scene.input.KeyCode; // Коды клавиш клавиатуры (нужен для клавиши Z в Ctrl+Z)
import javafx.scene.input.KeyCodeCombination; // Класс клавиатурных комбинаций (описываем Ctrl+Z)
import javafx.scene.input.KeyEvent; // Класс события нажатия клавиши
import javafx.scene.layout.VBox; // Вертикальный контейнер: элементы складываются сверху вниз
import javafx.stage.Stage; // Класс окна приложения

import java.util.ArrayDeque; // Реализация деке (двусторонней очереди) — используем как стек истории
import java.util.Deque; // Интерфейс деке: через него работаем со стеком вставок для Ctrl+Z

public class NamesForm { // Класс второй экранной формы — "Список имён" (задание 2)

    private final ListView<String> namesList = new ListView<>(); // Сам список имён (2.a): хранит и отображает элементы
    private final TextField nameField = new TextField(); // Поле ввода нового имени (вписать напрямую в список запрещено)
    private final Deque<String> addedHistory = new ArrayDeque<>(); // Стек добавленных имён — для отмены вставки по Ctrl+Z

    public static void open() { // Статический метод открытия формы — вызывается кнопкой из Main
        NamesForm form = new NamesForm(); // Создаём экземпляр нашей формы
        Stage stage = new Stage(); // Создаём НОВОЕ (второе) окно
        stage.setTitle("Демоэкзамен: список имён"); // Заголовок окна
        stage.setScene(form.buildScene()); // Собираем сцену формы и помещаем её в окно
        stage.show(); // Показываем окно на экране
    }

    private Scene buildScene() { // Метод собирает всю форму и возвращает готовую сцену
        namesList.getItems().addAll("Алексей", "Андрей", "Варвара"); // Наполняем список стартовыми именами (как в методичке)
        namesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Разрешаем выделять только один элемент списка
        namesList.setCellFactory(lv -> new NameCell()); // Подменяем ячейки списка на свои — ради подсветки (2.b.i)
        // 2.b.ii: после клика список в фокусе — стрелки вверх/вниз ходят по списку
        namesList.setOnMouseClicked(e -> namesList.requestFocus()); // По клику мышью передаём списку фокус клавиатуры

        Label title = new Label("Список имён:"); // Надпись-заголовок "Список имён:"
        Label prompt = new Label("Введите имя:"); // Надпись-подсказка "Введите имя:"

        Button addButton = new Button("Добавить в список"); // Кнопка добавления (задание 2.a)
        addButton.setOnAction(e -> addName()); // Обработчик клика: вызвать метод addName()

        Button deleteButton = new Button(); // Кнопка удаления (2.b) — без текста, только с картинкой
        ImageView icon = new ImageView(new Image(NamesForm.class.getResourceAsStream("/delete.png"))); // Читаем delete.png из папки resources и заворачиваем в ImageView
        icon.setFitWidth(24); // Масштабируем картинку до 24 px по ширине (исходник 96 px)
        icon.setFitHeight(24); // Масштабируем картинку до 24 px по высоте
        deleteButton.setGraphic(icon); // Вешаем картинку на кнопку — то самое "изображение кнопки удалить"
        deleteButton.setTooltip(new Tooltip("Удалить выбранный элемент")); // Всплывающая подсказка при наведении курсора
        deleteButton.setOnAction(e -> deleteSelected()); // Обработчик клика: удалить выделенный элемент

        Button checkButton = new Button("Проверить на символы"); // Кнопка проверки на "лишние" символы (задание 2.d)
        checkButton.setOnAction(e -> checkBadSymbols()); // Обработчик клика: вызвать метод checkBadSymbols()

        VBox box = new VBox(10, title, namesList, prompt, nameField, // Вертикальный контейнер с зазором 10 px: заголовок, список,
                addButton, deleteButton, checkButton); // подсказка, поле и три кнопки сверху вниз
        box.setPadding(new Insets(20)); // Отступ 20 px от краёв окна до содержимого

        Scene scene = new Scene(box, 360, 430); // Создаём сцену 360x430 px с нашим контейнером внутри
        // 2.b.iii: отмена последней вставки по Ctrl+Z
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> { // Перехватываем нажатия клавиш РАНЬШЕ всех элементов сцены
            if (new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN).match(e)) { // Проверяем: это комбинация Ctrl+Z?
                undoLastAdd(); // Да → отменяем последнюю вставку (убираем имя из списка)
                e.consume(); // "Съедаем" событие, чтобы никто больше на Ctrl+Z не реагировал
            }
        });
        return scene; // Возвращаем готовую сцену в метод open()
    }

    // 2.c: нормализация данных
    private String normalize(String raw) { // Метод приводит имя к каноническому виду
        String noSpaces = raw.replaceAll("\\s+", ""); // 2.c.i: удаляем ВСЕ пробелы — до, после и внутри слова
        if (noSpaces.isEmpty()) return noSpaces; // Защита: если после удаления ничего не осталось — возвращаем пустую строку
        // 2.c.ii / 2.c.iii / 2.c.iv: первая буква заглавная, остальные строчные
        return Character.toUpperCase(noSpaces.charAt(0)) // Первый символ делаем заглавным
                + noSpaces.substring(1).toLowerCase(); // и приклеиваем остаток слова в нижнем регистре
    }

    private void addName() { // Метод добавления имени (вызывается кнопкой "Добавить в список")
        String name = normalize(nameField.getText()); // Читаем текст поля и сразу нормализуем его
        if (name.isEmpty()) return; // Пустое поле → ничего не делаем, выходим
        // 2.e: проверка дубликатов
        if (namesList.getItems().contains(name)) { // Если такое имя УЖЕ есть в списке...
            Alert alert = new Alert(Alert.AlertType.INFORMATION, // ...создаём информационное модальное окно
                    "Уже есть в списке", ButtonType.OK); // с текстом "Уже есть в списке" и одной кнопкой ОК (2.e.i)
            alert.setHeaderText(null); // Убираем шапку окна — остаётся только чистый текст
            alert.showAndWait(); // Показываем окно и ждём, пока пользователь нажмёт ОК
            return; // 2.e.ii: вставка НЕ осуществляется — выходим из метода
        }
        namesList.getItems().add(name); // 2.a: добавляем имя в список (только через кнопку!)
        addedHistory.push(name); // Кладём имя на вершину стека истории (для отмены по Ctrl+Z)
        nameField.clear(); // Очищаем поле ввода под следующее имя
    }

    private void deleteSelected() { // Метод удаления (вызывается кнопкой с картинкой корзины)
        String selected = namesList.getSelectionModel().getSelectedItem(); // Берём текущий выделенный элемент списка
        if (selected == null) return; // Ничего не выделено → выходим, не удаляем
        namesList.getItems().remove(selected); // Удаляем выделенный элемент из списка (задание 2.b)
    }

    private void undoLastAdd() { // Метод отмены последней вставки (Ctrl+Z, задание 2.b.iii)
        String last = addedHistory.poll(); // Снимаем со стека последнее добавленное имя
        if (last != null) namesList.getItems().remove(last); // Если оно было — убираем это имя из списка
    }

    // 2.d: поиск невалидных символов
    private void checkBadSymbols() { // Метод проверки на "плохие" символы (вызывается кнопкой проверки)
        String text = nameField.getText(); // Читаем текущий текст из поля ввода
        if (text == null || text.isEmpty()) return; // Поле пустое → проверять нечего, выходим
        boolean hasBad = text.chars().anyMatch(ch -> // Проходим по каждому символу текста и спрашиваем:
                !Character.isLetter(ch) && !Character.isWhitespace(ch)); // это НЕ буква и НЕ пробел (цифра, скобка, знак)?
        if (!hasBad) return; // Плохих символов нет → молча выходим
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, // Создаём модальное окно подтверждения (2.d.i)
                "Внимание: Есть не текстовые символы, очистить?", // Текст окна — дословно как в методичке
                new ButtonType("Очистить"), new ButtonType("Оставить")); // Две кнопки ответа: "Очистить" и "Оставить"
        alert.setHeaderText(null); // Убираем шапку окна
        alert.showAndWait().ifPresent(type -> { // Показываем окно и ждём выбор пользователя
            if ("Очистить".equals(type.getText())) { // Если нажато "Очистить"...
                nameField.setText(text.replaceAll("[^\\p{L}\\s]", "")); // ...стираем из поля всё, кроме букв и пробелов
            } // Если нажато "Оставить" — ничего не делаем, текст остаётся как был
        });
    }

    // 2.b.i: подсветка выбранного элемента иным цветом
    private static class NameCell extends ListCell<String> { // Свой класс ячейки списка (наследуем стандартную ListCell)
        NameCell() { // Конструктор ячейки
            selectedProperty().addListener((o, old, sel) -> applyStyle()); // Подписка: изменилось выделение → перерисовать стиль
        }
        @Override // Переопределяем метод родительского класса
        protected void updateItem(String item, boolean empty) { // Вызывается каждый раз, когда ячейку нужно перерисовать
            super.updateItem(item, empty); // Обязательный вызов родительской логики (иначе глюки отображения)
            setText(empty || item == null ? null : item); // Пустая ячейка → без текста; иначе → показываем имя
            applyStyle(); // Применяем цвет фона
        }
        private void applyStyle() { // Метод применения цвета фона ячейки
            setStyle(!isEmpty() && isSelected() // Если ячейка не пустая И выделена...
                    ? "-fx-background-color: #ffe08a;" : ""); // ...красим в жёлтый #ffe08a, иначе — стиль по умолчанию
        }
    }
}