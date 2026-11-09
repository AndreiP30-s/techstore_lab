package com.example.techstore.repository.memory;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.example.techstore.model.user.User;
import com.example.techstore.repository.UserRepository;
@Repository
public class InMemoryUserRepository extends InMemoryRepository<UUID, User> implements UserRepository { }
