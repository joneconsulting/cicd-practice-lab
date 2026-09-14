package com.commercelab.productservice.model;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 이 강의는 CI/CD 파이프라인 실습에 집중하기 위해 데이터베이스 없이
 * 메모리 내 고정 목록만 사용합니다. 실무에서는 이 자리에 JPA Repository 등이 들어갑니다.
 */
@Repository
public class ProductRepository {

    private final List<Product> products = List.of(
            new Product(1L, "무선 키보드", 39000),
            new Product(2L, "노트북 거치대", 25000),
            new Product(3L, "블루투스 마우스", 18000)
    );

    public List<Product> findAll() {
        return products;
    }

    public Optional<Product> findById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }
}
