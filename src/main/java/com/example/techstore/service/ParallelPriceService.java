package com.example.techstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ParallelPriceService {
    private static final Logger log = LoggerFactory.getLogger(ParallelPriceService.class);

    public List<PriceQuote> calculate(
            Collection<PriceSnapshot> products, BigDecimal discountPercent, int threadCount) {
        Objects.requireNonNull(discountPercent, "discountPercent");
        if (discountPercent.signum() < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        if (threadCount < 1 || threadCount > 32) {
            throw new IllegalArgumentException("Thread count must be 1..32");
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new IllegalStateException("Price calculation interrupted");
        }
        List<PriceSnapshot> snapshot = List.copyOf(products);
        if (snapshot.isEmpty()) {
            return List.of();
        }
        AtomicInteger sequence = new AtomicInteger();
        ExecutorService executor =
                Executors.newFixedThreadPool(
                        Math.min(threadCount, snapshot.size()),
                        task -> new Thread(task, "price-worker-" + sequence.incrementAndGet()));
        try {
            List<Callable<PriceQuote>> tasks = new ArrayList<>();
            BigDecimal multiplier = BigDecimal.ONE.subtract(discountPercent.movePointLeft(2));
            for (PriceSnapshot product : snapshot) {
                BigDecimal originalPrice = product.price();
                tasks.add(
                        () -> {
                            BigDecimal discounted =
                                    originalPrice
                                            .multiply(multiplier)
                                            .setScale(2, RoundingMode.HALF_UP);
                            String threadName = Thread.currentThread().getName();
                            log.info(
                                    "Поток {}: {} — {} -> {} руб.",
                                    threadName,
                                    product.name(),
                                    originalPrice,
                                    discounted);
                            return new PriceQuote(
                                    product.article(), originalPrice, discounted, threadName);
                        });
            }
            List<PriceQuote> result = new ArrayList<>();
            for (Future<PriceQuote> future : executor.invokeAll(tasks)) {
                result.add(future.get());
            }
            return List.copyOf(result);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Price calculation interrupted", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Price calculation failed", exception.getCause());
        } finally {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Price workers did not stop in time");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
