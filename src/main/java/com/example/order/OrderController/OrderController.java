package com.example.order.OrderController;

import com.example.order.model.StockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.example.order.entity.Order;
import com.example.order.model.Product;
import com.example.order.repository.OrderRepository;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<java.lang.Object> createOrder(@RequestBody Order order) {
        // Récupérer les détails du produit à partir du microservice des produits
        String productServiceUrl = "http://Products/products/" + order.getProductId();
        Product product = restTemplate.getForObject(productServiceUrl, Product.class);

        // Définir le nom, le prix et le prix total de la commande
        if (product != null) {
            order.setProductName(product.getName());
            order.setPrice(product.getPrice());
            order.setTotalPrice(order.getQuantity() * product.getPrice());
        }
        else { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no product found");

        }
        boolean stockAvailable = checkStock(order.getProductId(), order.getQuantity());
        if (stockAvailable) {
            updateStock(order.getProductId(), order.getQuantity());
            Order savedOrder = orderRepository.save(order);
            return ResponseEntity.ok(savedOrder);
        }
        else
        {
          return   ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stock not sufficient for order creation");
    }
    }
    private void updateStock(String productId, int quantity) {
        StockDTO stockDTO = new StockDTO();
        stockDTO.setProductId(productId);
        stockDTO.setQuantity(quantity);

        String stockUrl = "http://Stock/stocks/updateStock";
        try {
            restTemplate.put(stockUrl, stockDTO);
        } catch (RestClientException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            throw new RuntimeException("Failed to update stock", e);
        }
    }
    private boolean checkStock(String productId, int quantity) {
        String stockUrl = "http://Stock/stocks/checkStock";
        StockDTO stockDTO = new StockDTO();
        stockDTO.setProductId(productId);
        stockDTO.setQuantity(quantity);
        return restTemplate.postForObject(stockUrl, stockDTO, Boolean.class);
    }
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable String id) {
        return orderRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable String id, @RequestBody Order orderDetails) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setProductId(orderDetails.getProductId());
            order.setProductName(orderDetails.getProductName());
            order.setQuantity(orderDetails.getQuantity());
            order.setPrice(orderDetails.getPrice());
            order.setTotalPrice(orderDetails.getTotalPrice());
            return orderRepository.save(order);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable String id) {
        orderRepository.deleteById(id);
    }
}
