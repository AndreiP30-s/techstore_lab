package com.example.techstore.console;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.model.product.HeadphoneType;
import com.example.techstore.presentation.ProductFormatter;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(5)
class ProductBrowserTest {
    private final StringWriter output = new StringWriter();
    private List<Product> products(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            products.add(new Headphones(100000L + i, "Sound-" + i,
                    "Brand", BigDecimal.TEN, HeadphoneType.IN_EAR, true, false, 20, 20000));
        }
        return products;
    }
    private Optional<Product> browse(List<Product> products, String input, boolean selecting) {
        ConsoleIO io = new ConsoleIO(new StringReader(input), new PrintWriter(output));
        return new ProductBrowser(io, new ProductFormatter()).browse(products, selecting);
    }

    // На первой странице видны 10 из 101 товара без характеристик.
    @Test
    void firstPageShowsTenOf101ProductsWithoutCharacteristics() {
        List<Product> products = products(101);
        browse(products, "0\n", false);
        String text = output.toString();
        assertTrue(text.contains("Страница 1/11"));
        assertTrue(text.contains("10. 100010 |"));
        assertFalse(text.contains("11. 100011 |"));
        assertFalse(text.contains("Диапазон частот"));
    }

    // Последняя страница и открытие карточки.
    @Test
    void lastPageContainsRemainderAndDetailsOpenOnlyOnRequest() {
        browse(products(101), "n\n".repeat(10) + "101\n\n0\n", false);
        String lastPage = output.toString().substring(output.toString().indexOf("Страница 11/11"));
        assertTrue(lastPage.contains("101. 100101 |"));
        assertFalse(lastPage.contains("100. 100100 |"));
        assertTrue(lastPage.contains("Артикул: 100101"));
        assertTrue(lastPage.contains("Диапазон частот: 20–20000 Гц"));
    }

    // Выбор на текущей странице и неверные команды.
    @Test
    void selectionUsesVisiblePageAndHandlesOutOfRangeInput() {
        List<Product> products = products(12);
        Product selected = browse(products, "p\n11\nwrong\nn\nn\n11\n", true).orElseThrow();
        assertSame(products.get(10), selected);
        assertTrue(output.toString().contains("Это первая страница"));
        assertTrue(output.toString().contains("Это последняя страница"));
        assertTrue(output.toString().contains("Введите номер с текущей страницы"));
    }

    // Пустой список и отмена выбора.
    @Test
    void handlesEmptyListAndCancellation() {
        assertTrue(browse(List.of(), "", true).isEmpty());
        assertTrue(browse(products(1), "0\n", true).isEmpty());
    }
}
