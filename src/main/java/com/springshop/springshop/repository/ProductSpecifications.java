package com.springshop.springshop.repository;

import com.springshop.springshop.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> nameContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String escaped = q.trim().toLowerCase()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            return cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '\\');
        };
    }
}
