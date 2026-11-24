package com.example.techstore.service.impl;

import com.example.techstore.exception.DiscountConfigurationException;
import com.example.techstore.exception.DiscountStateException;
import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.ValidationException;
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
        DiscountState state = states.findById(1).orElseThrow(DiscountConfigurationException::new);
        return new Status(state.isActive());
    }

    @Override
    public Result apply(BigDecimal percent, int threads) {
        DiscountState state = lock();
        if (state.isActive())
            throw new DiscountStateException(
                    ErrorCode.DISCOUNT_ALREADY_ACTIVE,
                    "Скидка уже действует");
        List<Product> entities = products.findAll();
        if (entities.isEmpty())
            throw new DiscountStateException(ErrorCode.EMPTY_CATALOG, "Каталог пуст");
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
            throw new ValidationException(
                    ErrorCode.INVALID_DISCOUNT_RESULT,
                    "После скидки цена должна быть больше нуля");
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
        if (!state.isActive())
            throw new DiscountStateException(ErrorCode.NO_ACTIVE_DISCOUNT, "Нет действующей скидки");
        List<DiscountBackup> prices = backups.findAll();
        for (DiscountBackup backup : prices) {
            Product product =
                    products.findById(backup.getProductArticle())
                            .orElseThrow(() -> new DiscountStateException(ErrorCode.DISCOUNT_PRODUCT_NOT_FOUND, "Товар скидки отсутствует"));
            product.setPrice(backup.getOriginalPrice());
        }
        backups.deleteAllInBatch();
        state.setActive(false);
        products.flush();
        return new Result(prices.size());
    }

    private DiscountState lock() {
        return states.lockState().orElseThrow(DiscountConfigurationException::new);
    }
}
