package com.springshop.springshop.controller;

import com.springshop.springshop.dto.PurchaseRequest;
import com.springshop.springshop.dto.PurchaseResponse;
import com.springshop.springshop.entity.enums.PurchaseStatus;
import com.springshop.springshop.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody PurchaseRequest request, Authentication authentication) {
        return purchaseService.create(request, authentication.getName());
    }

    @GetMapping
    public List<PurchaseResponse> list() {
        return purchaseService.findAll();
    }

    @GetMapping("/me")
    public List<PurchaseResponse> myPurchases(Authentication authentication) {
        return purchaseService.findByUser(authentication.getName());
    }

    @GetMapping("/{id}")
    public PurchaseResponse get(@PathVariable Long id) {
        return purchaseService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public PurchaseResponse updateStatus(@PathVariable Long id, @RequestParam PurchaseStatus status) {
        return purchaseService.updateStatus(id, status);
    }
}
