package com.example.techstore.console;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(5)
class ConsoleIOTest {
    private ConsoleIO input(String script) {
        return new ConsoleIO(new StringReader(script), new PrintWriter(new StringWriter()));
    }

    // После неверной суммы можно ввести правильную.
    @Test
    void moneyRejectsExtremeExponentsAndRecovers() {
        ConsoleIO io = input("1e2147483647\n1e-2147483647\n9999999999999999\n-10\n1.001\n99,90\n");
        assertEquals(new BigDecimal("99.90"), io.money("Цена:"));
    }

    // После неверного процента можно ввести правильный.
    @Test
    void percentRejectsExtremeExponentsAndRecovers() {
        ConsoleIO io = input("1e-2147483647\n1e2147483647\n101\n-1\n10,25\n");
        assertEquals(new BigDecimal("10.25"), io.percent("Скидка:"));
    }

    // Конец ввода после ошибки не вызывает цикл.
    @Test
    void invalidValueFollowedByEofExitsInsteadOfLooping() {
        ConsoleIO io = input("not-a-number\n");
        assertThrows(EndOfInputException.class, () -> io.money("Цена:"));
    }

    // После ошибки ввода работает /cancel.
    @Test
    void invalidValueDoesNotPreventCancellation() {
        ConsoleIO io = input("1e2147483647\n/cancel\n");
        assertThrows(InputCancelledException.class, () -> io.money("Цена:"));
    }
}
