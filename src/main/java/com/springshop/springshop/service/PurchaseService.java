package com.springshop.springshop.service;

import com.springshop.springshop.dto.DetailRequest;
import com.springshop.springshop.dto.DetailResponse;
import com.springshop.springshop.dto.PurchaseRequest;
import com.springshop.springshop.dto.PurchaseResponse;
import com.springshop.springshop.entity.Detail;
import com.springshop.springshop.entity.Product;
import com.springshop.springshop.entity.Purchase;
import com.springshop.springshop.entity.User;
import com.springshop.springshop.entity.enums.PurchaseStatus;
import com.springshop.springshop.exception.BusinessException;
import com.springshop.springshop.exception.NotFoundException;
import com.springshop.springshop.repository.ProductRepository;
import com.springshop.springshop.repository.PurchaseRepository;
import com.springshop.springshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public PurchaseResponse create(PurchaseRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Purchase purchase = Purchase.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .shippingAddress(request.shippingAddress())
                .user(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (DetailRequest line : request.items()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("Produuit introuvabel" + line.productId()));

            if (product.getStock() < line.quantity()) {
                throw new BusinessException("Stock insuffisant pour " + product.getName() + " (dispo : " + line.quantity());
            }
            product.setStock(product.getStock() - line.quantity());

            purchase.addItem(Detail.builder()
                    .product(product)
                    .quantity(line.quantity())
                    .unitPrice(product.getPrice())
                    .build());

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        purchase.setTotal(total);
        return toResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public PurchaseResponse updateStatus(Long id, PurchaseStatus status) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commande introuvable " + id));
        purchase.setStatus(status);
        return toResponse(purchase);
    }

    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<PurchaseResponse> findByUser(String email) {
        return purchaseRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(this::toResponse).toList();
    }

    public PurchaseResponse findById(Long id) {
        return purchaseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Commande introuvable" + id));
    }

    private PurchaseResponse toResponse(Purchase p) {
        List<DetailResponse> items = p.getItems().stream()
                .map(i -> new DetailResponse(
                        i.getProduct().getId(), i.getProduct().getName(), i.getQuantity(), i.getUnitPrice()
                )).toList();
        return new PurchaseResponse(p.getId(), p.getCustomerName(), p.getCustomerEmail(), p.getShippingAddress(), p.getStatus(), p.getCreatedAt(), p.getTotal(), items);
    }
}
