package com.commercelab.productservice.controller;

import com.commercelab.productservice.dto.ProductListResponse;
import com.commercelab.productservice.model.Product;
import com.commercelab.productservice.model.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProductController {

    private final ProductRepository repository;

    // Section 3~9 전체 실습에서 배포 버전을 눈으로 확인하는 데 쓰는 값입니다.
    // application.yml의 app.version, 또는 APP_VERSION 환경변수로 덮어쓸 수 있습니다.
    @Value("${app.version:v1}")
    private String version;

    // Section 9 장애 주입 드릴(Chaos Drill) 전용 스위치입니다.
    // true로 켜면 /api/products가 500을 반환합니다 — 코드를 고치지 않고
    // application.yml 값(또는 APP_SIMULATE_ERROR 환경변수)만 바꿔 장애를 재현할 수 있습니다.
    @Value("${app.simulate-error:false}")
    private boolean simulateError;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/products")
    public ProductListResponse getProducts() {
        if (simulateError) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "장애 주입 드릴: app.simulate-error=true 상태입니다 (Section 9 참고)");
        }
        return new ProductListResponse(version, repository.findAll());
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

