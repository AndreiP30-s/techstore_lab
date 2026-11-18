package com.example.techstore.service.impl;

import com.example.techstore.exception.ConflictException;
import com.example.techstore.model.discount.DiscountBackup;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.Product;
import com.example.techstore.repository.DiscountBackupRepository;
import com.example.techstore.repository.DiscountStateRepository;
import com.example.techstore.repository.ProductRepository;
import com.example.techstore.service.DiscountService;
import com.example.techstore.service.ParallelPriceService;
import com.example.techstore.service.PriceQuote;
import com.example.techstore.service.PriceSnapshot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DiscountServiceImpl implements DiscountService {
    private final ProductRepository products;
    private final DiscountStateRepository states;
    private final DiscountBackupRepository backups;
    private final ParallelPriceService calculator;

    public DiscountServiceImpl(
            ProductRepository products,
            DiscountStateRepository states,
            DiscountBackupRepository backups,
            ParallelPriceService calculator) {
        this.products = products;
        this.states = states;
        this.backups = backups;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    @Override
    public Status status() {
        return new Status(states.findById(1).orElseThrow().isActive());
    }

    @Override
    public Result apply(BigDecimal percent, int threads) {
        DiscountState state = lock();
        if (state.isActive()) throw new ConflictException("Сначала отмените действующую скидку");
        List<Product> entities = products.findAll();
        if (entities.isEmpty()) throw new IllegalArgumentException("Каталог пуст");
        List<PriceSnapshot> snapshot =
                entities.stream()
                        .map(
                                product ->
                                        new PriceSnapshot(
                                                product.getArticle(),
                                                product.getName(),
                                                product.getPrice()))
                        .toList();
        List<PriceQuote> prices = calculator.calculate(snapshot, percent, threads);
        if (prices.stream().anyMatch(p -> p.discountedPrice().signum() <= 0))
            throw new IllegalArgumentException("После скидки цена должна быть больше нуля");
        Map<Integer, BigDecimal> byArticle = new HashMap<>();
        for (PriceQuote quote : prices) byArticle.put(quote.article(), quote.discountedPrice());
        for (Product product : entities) {
            backups.save(new DiscountBackup(product.getArticle(), product.getPrice()));
            product.setPrice(byArticle.get(product.getArticle()));
        }
        state.setActive(true);
        products.flush();
        return new Result(entities.size());
    }

    @Override
    public Result cancel() {
        DiscountState state = lock();
        if (!state.isActive()) throw new ConflictException("Нет действующей скидки");
        List<DiscountBackup> prices = backups.findAll();
        for (DiscountBackup backup : prices) {
            Product product =
                    products.findById(backup.getProductArticle())
                            .orElseThrow(() -> new ConflictException("Товар скидки отсутствует"));
            product.setPrice(backup.getOriginalPrice());
        }
        backups.deleteAllInBatch();
        state.setActive(false);
        products.flush();
        return new Result(prices.size());
    }

    private DiscountState lock() {
        return states.lockState()
                .orElseThrow(() -> new IllegalStateException("Выполните db/02-java-support.sql"));
    }
}
