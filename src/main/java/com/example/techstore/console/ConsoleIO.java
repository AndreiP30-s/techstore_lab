package com.example.techstore.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import com.example.techstore.model.Validation;

public class ConsoleIO {
    private final BufferedReader input;
    private final PrintWriter output;
    public ConsoleIO(Reader input, PrintWriter output) {
        this.input = new BufferedReader(input);
        this.output = output;
    }

    public void println(String text) { output.println(text); output.flush(); }

    public String line(String prompt) { return rawLine(prompt).strip(); }

    private String rawLine(String prompt) {
        output.print(prompt + " ");
        output.flush();
        try {
            String value = input.readLine();
            if (value == null) { throw new EndOfInputException(); }
            if (value.strip().equalsIgnoreCase("/cancel")) { throw new InputCancelledException(); }
            return value;
        } catch (IOException exception) { throw new UncheckedIOException(exception); }
    }

    private <T> T read(String prompt, Function<String, T> parser, String error) {
        while (true) {
            String value = line(prompt);
            try { return parser.apply(value); }
            catch (IllegalArgumentException exception) { println(error); }
        }
    }

    public String text(String prompt) {
        return read(prompt, value -> Validation.text(value, "text"), "Введите непустой текст.");
    }

    public boolean menu(String title, List<MenuItem> items, String exitLabel) {
        println("\n" + title);
        for (int i = 0; i < items.size(); i++) { println((i + 1) + ". " + items.get(i).label()); }
        println("0. " + exitLabel);
        int choice = integer("Выберите пункт:", 0, items.size());
        if (choice == 0) { return false; }
        items.get(choice - 1).action().run();
        return true;
    }

    public int integer(String prompt, int min, int max) {
        return read(prompt, value -> {
            int number = Integer.parseInt(value);
            if (number < min || number > max) { throw new IllegalArgumentException(); }
            return number;
        }, "Введите целое число от " + min + " до " + max + ".");
    }

    public int positiveInt(String prompt) { return integer(prompt, 1, Integer.MAX_VALUE); }

    public double positiveDouble(String prompt) {
        return read(prompt, value -> Validation.positive(Double.parseDouble(value.replace(',', '.')), "number"),
                "Введите конечное число больше нуля, например 6,2.");
    }

    // Не принимаем экспоненциальную запись: огромная степень может вызвать переполнение.
    private BigDecimal decimal(String value) {
        if (!value.matches("[0-9]{1,15}([.,][0-9]{1,2})?")) {
            throw new IllegalArgumentException("Use ordinary decimal notation");
        }
        return new BigDecimal(value.replace(',', '.'));
    }

    public BigDecimal money(String prompt) {
        return read(prompt, value -> Validation.price(decimal(value)),
                "Введите сумму: до 15 цифр перед запятой и до 2 после, без экспоненты.");
    }

    public BigDecimal percent(String prompt) {
        return read(prompt, value -> {
            BigDecimal number = decimal(value);
            if (number.signum() < 0 || number.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException();
            }
            return number;
        }, "Введите процент от 0 до 100, до 2 знаков после запятой, без экспоненты.");
    }

    public boolean yesNo(String prompt) {
        return read(prompt + " (да/нет):", value -> {
            if (value.equalsIgnoreCase("да") || value.equalsIgnoreCase("yes") || value.equals("1")) { return true; }
            if (value.equalsIgnoreCase("нет") || value.equalsIgnoreCase("no") || value.equals("0")) { return false; }
            throw new IllegalArgumentException();
        }, "Ответьте да или нет.");
    }

    public <T> T choose(String prompt, List<T> options, Function<T, String> label) {
        println(prompt);
        for (int i = 0; i < options.size(); i++) { println((i + 1) + ". " + label.apply(options.get(i))); }
        return options.get(integer("Номер:", 1, options.size()) - 1);
    }
}
