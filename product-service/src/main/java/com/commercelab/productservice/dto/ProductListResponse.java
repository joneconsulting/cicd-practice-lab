package com.commercelab.productservice.dto;

import com.commercelab.productservice.model.Product;
import java.util.List;

/**
 * "version" 필드가 이 강의 전체(Section 3, 5, 6, 7, 9)에서 배포된 버전을
 * 화면/커맨드에서 바로 확인하는 데 쓰입니다. application.yml의 app.version 값을
 * 그대로 실어 보냅니다.
 */
public class ProductListResponse {

    private final String version;
    private final List<Product> products;

    public ProductListResponse(String version, List<Product> products) {
        this.version = version;
        this.products = products;
    }

    public String getVersion() {
        return version;
    }

    public List<Product> getProducts() {
        return products;
    }
}
