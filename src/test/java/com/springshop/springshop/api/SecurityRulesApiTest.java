package com.springshop.springshop.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityRulesApiTest extends ApiTestSupport {
    private static String categoryJson() {
        return "{\"name\":\"Livres " + System.nanoTime() + "\"}";
    }

    @Test
    void testReadTheCatalogShouldBeOk() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
        mockMvc.perform(get("/api/categories")).andExpect(status().isOk());
    }

    @Test
    void testCreateCategoryWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCategoryWithInvalidTokenShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer("ceci.nest.pas-un-jwt"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCategoryWhenItsASimpleUserShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(newUserToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateCategoryWhenItsAdminShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void testSeePurchasesShouldAskForAConnexion() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/purchases/me"))
                .andExpect(status().isUnauthorized());
    }

    void testWhenAdminAskToSeeAllThePurchasesShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/purchases")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/purchases").header("Authorization", bearer(newUserToken())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/purchases").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());
    }
}
