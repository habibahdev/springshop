package com.springshop.springshop.service;

import com.springshop.springshop.dto.DetailRequest;
import com.springshop.springshop.dto.PurchaseRequest;
import com.springshop.springshop.dto.PurchaseResponse;
import com.springshop.springshop.entity.Product;
import com.springshop.springshop.entity.Purchase;
import com.springshop.springshop.entity.User;
import com.springshop.springshop.entity.enums.PurchaseStatus;
import com.springshop.springshop.entity.enums.Role;
import com.springshop.springshop.exception.BusinessException;
import com.springshop.springshop.exception.NotFoundException;
import com.springshop.springshop.repository.ProductRepository;
import com.springshop.springshop.repository.PurchaseRepository;
import com.springshop.springshop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTest {
    private static final String EMAIL = "alicetest@springboot.com";

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void tetComputeTotalAndFixPriceAndDecreaseStock() {
        Product book = product(1L, "Clean code", "35.90", 10);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildAdliceTest()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(book));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse purchaseResponse = purchaseService.create(request(new DetailRequest(1L, 2)), EMAIL);

        assertThat(purchaseResponse.status()).isEqualTo(PurchaseStatus.PENDING);
        assertThat(purchaseResponse.total()).isEqualByComparingTo("71.80");
        assertThat(purchaseResponse.items()).hasSize(1);
        assertThat(purchaseResponse.items().getFirst().unitPrice()).isEqualByComparingTo("35.90");
        assertThat(purchaseResponse.items().getFirst().quantity()).isEqualTo(2);
        assertThat(book.getStock()).isEqualTo(8);
    }

    @Test
    void testAddSeveralSubtotalPurchase() {
        Product pen = product(1L, "Stylo", "10.00", 20);
        Product notebook = product(2L, "Cahier", "4.50", 20);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildAdliceTest()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(pen));
        when(productRepository.findById(2L)).thenReturn(Optional.of(notebook));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse purchaseResponse = purchaseService.create(request(new DetailRequest(1L, 3), new DetailRequest(2L, 2)), EMAIL);

        assertThat(purchaseResponse.total()).isEqualByComparingTo("39.00");
        assertThat(purchaseResponse.items()).hasSize(2);
        assertThat(pen.getStock()).isEqualTo(17);
        assertThat(notebook.getStock()).isEqualTo(18);
    }

    @Test
    void testAssociatePurchaseToTheCustomer() {
        User user = buildAdliceTest();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L, "Clean code", "35.90", 10)));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        purchaseService.create(request(new DetailRequest(1L, 1)), EMAIL);
        ArgumentCaptor<Purchase> saved = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(user);
        assertThat(saved.getValue().getShippingAddress()).isEqualTo("1 rue de Paris");
    }

    @Test
    void testLowStockShouldThrowExceptionWithoutAnyOperationOnStock() {
        Product book = product(1L, "Clean code", "35.90", 1);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildAdliceTest()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> purchaseService.create(request(new DetailRequest(1L, 2)), EMAIL))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuffisant");
        assertThat(book.getStock()).isEqualTo(1);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void testCreateUnknownProductShouldThrowNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildAdliceTest()));
        when(productRepository.findById(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> purchaseService.create(request(new DetailRequest(4L, 1)), EMAIL))
                .isInstanceOf(NotFoundException.class);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void tesCreateUnkownUserShouldThrowNotFoundExceptionWithoutTouchingTheProduct() {
        when(userRepository.findByEmail("unkownuser@springboot.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> purchaseService.create(request(new DetailRequest(1L, 1)), "unkownuser@springboot.com"))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void testUpdateStatus() {
        Purchase purchase = Purchase.builder().id(5L).customerName("Alice").customerEmail(EMAIL)
                .shippingAddress("1 rue de Paris").total(BigDecimal.TEN).build();
        when(purchaseRepository.findById(5L)).thenReturn(Optional.of(purchase));
        PurchaseResponse purchaseResponse = purchaseService.updateStatus(5L, PurchaseStatus.SHIPPED);

        assertThat(purchaseResponse.status()).isEqualTo(PurchaseStatus.SHIPPED);
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.SHIPPED);
    }

    @Test
    void testUpdateStatusOfUnknownProductShouldThrowNotFoundException() {
        when(purchaseRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> purchaseService.updateStatus(7L, PurchaseStatus.PAID))
                .isInstanceOf(NotFoundException.class);
    }

    private static Product product(long id, String name, String price, int stock) {
        return Product.builder().id(id).name(name).price(new BigDecimal(price)).stock(stock).build();
    }

    private static User buildAdliceTest() {
    return User.builder().id(1L).email(EMAIL).fullName("Alice").password("x").role(Role.USER).build();
    }

    private static PurchaseRequest request(DetailRequest... items) {
        return new PurchaseRequest("Alice", EMAIL, "1 rue de Paris", List.of(items));
    }
}
