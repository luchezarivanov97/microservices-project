package com.example.product_service.service;

import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final CurrencyService currencyService;

    public ProductService(ProductRepository productRepository, CurrencyService currencyService) {
        this.productRepository = productRepository;
        this.currencyService = currencyService;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow();
    }
    public List<Product> getByIds(List<Long> ids) {
        return productRepository.findAllById(ids);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void reduceProductQuantity(Long productId, int quantityToReduce) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < quantityToReduce) {
            throw new RuntimeException("Not enough stock for product ID " + productId);
        }

        product.setQuantity(product.getQuantity() - quantityToReduce);
        productRepository.save(product);
    }

    public ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        );
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    public Double getTotalPriceInBGN(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);

        Map<String, Double> totalPricePerCurrency = new HashMap<>();

        for (Product product : products) {
            Map<String, Double> priceMap = product.getPrice();
            for (Map.Entry<String, Double> entry : priceMap.entrySet()) {
                String currency = entry.getKey();
                Double amount = entry.getValue();

                totalPricePerCurrency.merge(currency, amount, Double::sum);
            }
        }

        Map<String, Double> convertedMap = currencyService.convertAllToBGN(totalPricePerCurrency);
        Double bgnPrice = convertedMap.get("BGN");

        if (bgnPrice == null) {
            throw new RuntimeException("BGN conversion failed or missing");
        }

        return bgnPrice;
    }

}
