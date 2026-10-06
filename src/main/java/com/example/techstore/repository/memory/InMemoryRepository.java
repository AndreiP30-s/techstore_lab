package com.example.techstore.repository.memory;
import com.example.techstore.model.Identifiable;
import com.example.techstore.repository.Repository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
/** Методы синхронизированы. findAll возвращает копию списка. */
public abstract class InMemoryRepository<K, T extends Identifiable<K>> implements Repository<K, T> {
    private final Map<K, T> entities = new LinkedHashMap<>();
    @Override public synchronized void add(T entity) {
        Objects.requireNonNull(entity, "entity");
        if (entities.putIfAbsent(entity.getId(), entity) != null) {
            throw new IllegalArgumentException("Duplicate id: " + entity.getId());
        }
    }
    @Override public synchronized Optional<T> findById(K id) {
        return Optional.ofNullable(entities.get(Objects.requireNonNull(id, "id")));
    }
    @Override public synchronized List<T> findAll() { return List.copyOf(entities.values()); }
    @Override public synchronized boolean deleteById(K id) {
        return entities.remove(Objects.requireNonNull(id, "id")) != null;
    }
}
