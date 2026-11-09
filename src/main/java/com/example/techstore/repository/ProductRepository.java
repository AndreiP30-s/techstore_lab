package com.example.techstore.repository;
import com.example.techstore.model.product.Product;
public interface ProductRepository extends Repository<Long, Product> {
    long nextArticle();
}
