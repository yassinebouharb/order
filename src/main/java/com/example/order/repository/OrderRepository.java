package com.example.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.order.entity.Order;
import org.springframework.stereotype.Repository;
@Repository // Add this annotation

public interface OrderRepository extends MongoRepository<Order, String> {
}
