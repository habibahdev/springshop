package com.springshop.springshop.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseApiTest extends ApiTestSupport {
    private final String tag = UUID.randomUUID().toString().substring(0, 8);

    private static String purchaseJson(long productId, int quantity) {
        return """
                {"customerName":"Alice","customerEmail":"alicetest@springboot.com","shippingAddress":"1 rue de Paris",
                "items":[{"productId":%d,"quantity":%d}]}
                """.formatted(productId, quantity);
    }

    @Test
    void testComputeTotalAndFixPriceAndDecreaseStock() throws Exception {
        String admin = adminToken();
        long productId = createProduct(admin, "Clean Code " + tag, "35.90", 10, null);
        String user = newUserToken();

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(productId, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(71.80))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Clean Code " + tag))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(35.90));

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void testLowStockShouldThrowExceptionWithoutAnyOperationOnStock() throws Exception {
        String admin = adminToken();
        long productId = createProduct(admin, "Rare " + tag, "10.00", 3, null);

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", bearer(newUserToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(productId, 10)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("Stock insuffisant")));

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void testPurchaseNotExistingProductShouldThrowsNotFoundException() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", bearer(newUserToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(999999, 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRefusingInvalidPurchase() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", bearer(newUserToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"","customerEmail":"pas-un-email","shippingAddress":"","items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerEmail").exists())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void testWhenACustomerAskToSeePurchasesShouldReturnOnlyHisOwn() throws Exception {
        String admin = adminToken();
        long productId = createProduct(admin, "Partage " + tag, "10.00", 10, null);
        String alice = newUserToken();
        String bob = newUserToken();

        mockMvc.perform(post("/api/purchases").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(purchaseJson(productId, 1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/purchases").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(purchaseJson(productId, 2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/purchases/me").header("Authorization", bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/purchases/me").header("Authorization", bearer(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testOnlyAdminCanChangeTheStatusOfThePurchase() throws Exception {
        String admin = adminToken();
        long productId = createProduct(admin, "Statut " + tag, "10.00", 10, null);
        String user = newUserToken();

        String response = mockMvc.perform(post("/api/purchases").header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON).content(purchaseJson(productId, 1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number orderId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        // un simple utilisateur ne peut pas
        mockMvc.perform(patch("/api/purchases/" + orderId + "/status").param("status", "PAID")
                        .header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());

        // l'admin, oui
        mockMvc.perform(patch("/api/purchases/" + orderId + "/status").param("status", "PAID")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
