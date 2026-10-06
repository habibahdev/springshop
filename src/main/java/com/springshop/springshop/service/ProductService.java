package com.springshop.springshop.service;

import com.springshop.springshop.dto.ProductRequest;
import com.springshop.springshop.dto.ProductResponse;
import com.springshop.springshop.entity.Category;
import com.springshop.springshop.entity.Product;
import com.springshop.springshop.exception.NotFoundException;
import com.springshop.springshop.repository.CategoryRepository;
import com.springshop.springshop.repository.ProductRepository;
import com.springshop.springshop.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> search(Long categoryId, String q, Pageable pageable) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.hasCategory(categoryId),
                ProductSpecifications.nameContains(q)
        );
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public ProductResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        apply(product, request);
        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Produit introuvable " + id);
        }
        productRepository.deleteById(id);
        productRepository.flush();
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produci introuvable " + id));
    }

    private void apply(Product product, ProductRequest r) {
        product.setName(r.name());
        product.setDescription(r.description());
        product.setPrice(r.price());
        product.setStock(r.stock());
        product.setImageUrl(r.imageUrl());
        if (r.categoryId() != null) {
            Category category = categoryRepository.findById(r.categoryId())
                    .orElseThrow(() -> new NotFoundException("Catégorie introuvable " + r.categoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
    }

    private ProductResponse toResponse(Product p) {
        Category c = p.getCategory();
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(), p.getImageUrl(),
                c != null ? c.getId() : null,
                c != null ? c.getName() : null
        );
    }
}
