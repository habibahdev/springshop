package com.springshop.springshop.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogApiTest extends ApiTestSupport {
    private final String tag = UUID.randomUUID().toString().substring(0, 8);

    @Test
    void testCreateAProductAndSeeIt() throws Exception {
        String admin = adminToken();
        long categoryId = createCategory(admin, "Livres-" + tag);
        long productId = createProduct(admin, "Clean Code " + tag, "35.90", 10, categoryId);

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Clean Code " + tag))
                .andExpect(jsonPath("$.price").value(35.90))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.categoryName").value("Livres-" + tag));
    }

    @Test
    void testSeeAProductThatDoesntExistingShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInvalidProductShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","price":-5,"stock":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    void testSearchProductAndCategory() throws Exception {
        String admin = adminToken();
        long books = createCategory(admin, "Livres-" + tag);
        long games = createCategory(admin, "Jeux-" + tag);
        createProduct(admin, "Alpha livre " + tag, "10.00", 5, books);
        createProduct(admin, "Alpha jeu " + tag, "20.00", 5, games);
        createProduct(admin, "Beta livre " + tag, "30.00", 5, books);

        // nom seul : les deux « Alpha »
        mockMvc.perform(get("/api/products").param("q", "alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // catégorie seule : les deux livres
        mockMvc.perform(get("/api/products").param("categoryId", String.valueOf(books)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // nom + catégorie : un seul produit
        mockMvc.perform(get("/api/products")
                        .param("q", "alpha")
                        .param("categoryId", String.valueOf(books)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Alpha livre " + tag));
    }

    @Test
    void testSearchWithoutTheCasse() throws Exception {
        String admin = adminToken();
        createProduct(admin, "Souris " + tag, "15.00", 5, null);
        createProduct(admin, "Clavier " + tag, "45.00", 5, null);

        mockMvc.perform(get("/api/products").param("q", "SOURIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/products").param("q", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void testProductPagination() throws Exception {
        String admin = adminToken();
        for (int i = 1; i <= 3; i++) {
            createProduct(admin, "Pagine " + i + " " + tag, "5.00", 1, null);
        }

        mockMvc.perform(get("/api/products").param("q", "pagine").param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void testDeletingProductIsOnlyForAdmin() throws Exception {
        String admin = adminToken();
        long id = createProduct(admin, "A supprimer " + tag, "5.00", 1, null);

        mockMvc.perform(delete("/api/products/" + id).header("Authorization", bearer(newUserToken())))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/products/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }
}
