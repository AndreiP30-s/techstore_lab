package com.example.techstore.repository;
import com.example.techstore.model.Identifiable;
import java.util.List;
import java.util.Optional;
public interface Repository<K, T extends Identifiable<K>> {
    void add(T entity);
    Optional<T> findById(K id);
    List<T> findAll();
    boolean deleteById(K id);
}
