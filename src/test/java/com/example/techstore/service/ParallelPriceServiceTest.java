package com.example.techstore.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.PriceCalculationException;
import com.example.techstore.exception.ValidationException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

class ParallelPriceServiceTest {
    private final ParallelPriceService service = new ParallelPriceService();

    @Test
    void calculatesWithoutChangingSnapshotsAndNamesWorkers() {
        List<PriceSnapshot> input =
                IntStream.range(1, 101)
                        .mapToObj(
                                i -> new PriceSnapshot(i, "Product " + i, new BigDecimal("100.00")))
                        .toList();
        List<PriceQuote> result = service.calculate(input, BigDecimal.TEN, 3);
        assertEquals(input.size(), result.size());
        assertTrue(
                result.stream().allMatch(q -> q.discountedPrice().equals(new BigDecimal("90.00"))));
        assertTrue(result.stream().allMatch(q -> q.threadName().startsWith("price-worker-")));
        assertTrue(result.stream().map(PriceQuote::threadName).distinct().count() > 1);
        assertTrue(input.stream().allMatch(p -> p.price().equals(new BigDecimal("100.00"))));
    }

    @Test
    void validatesWorkerCountAndRoundsMoney() {
        List<PriceSnapshot> input =
                List.of(new PriceSnapshot(1, "Product", new BigDecimal("10.05")));
        assertEquals(
                new BigDecimal("9.05"),
                service.calculate(input, BigDecimal.TEN, 1).get(0).discountedPrice());
        assertThrows(
                ValidationException.class, () -> service.calculate(input, BigDecimal.TEN, 0));
        assertThrows(
                ValidationException.class, () -> service.calculate(input, BigDecimal.TEN, 33));
        assertThrows(
                ValidationException.class,
                () -> service.calculate(input, new BigDecimal("101"), 1));
    }

    @Test
    void preservesTheFailureFromTheWorker() {
        List<PriceSnapshot> input = List.of(new PriceSnapshot(1, "Product", null));
        PriceCalculationException error = assertThrows(
                PriceCalculationException.class, () -> service.calculate(input, BigDecimal.TEN, 1));
        assertEquals(ErrorCode.PRICE_CALCULATION_FAILED, error.getCode());
        assertInstanceOf(NullPointerException.class, error.getCause());
    }

    @Test
    void retainsTheInterruptFlag() {
        List<PriceSnapshot> input = List.of(new PriceSnapshot(1, "Product", BigDecimal.TEN));
        Thread.currentThread().interrupt();
        try {
            assertThrows(
                    PriceCalculationException.class, () -> service.calculate(input, BigDecimal.TEN, 1));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
