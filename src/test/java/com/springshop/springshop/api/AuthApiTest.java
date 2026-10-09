package com.springshop.springshop.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends ApiTestSupport {

    @Test
    void testRegisterShouldReturnToken() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"fullName":"Alice", "email":"%s","password":"motdepasse123"}
                                """.formatted(email)
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testRegisterWithEmailAlreadyInUseShouldReturnErrorMessage() throws Exception {
        String email = uniqueEmail();
        registerUser(email);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"fullName":"Alice", "email":"%s","password":"motdepasse123"}
                                """.formatted(email)
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("déjà utilisé")));
    }

    @Test
    void testRegisterWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"fullName":"Alice", "email":"email-de-test","password":"mdp"}
                                """
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void testLoginSuccessShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"email":"%s","password":"%s"}
                                """.formatted(adminEmail, adminPassword)
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testLoginShouldReturnErrorWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {"email":"%s","password":"mauvas-mot-de-apasse"}
                                """.formatted(adminEmail)
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect"));
    }

    @Test
    void testLoginShouldNotWorkWithUnknownUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {"email":"%s","password":"motdepasse123"}
                                        """.formatted(uniqueEmail())
                        ))
                .andExpect(status().isUnauthorized());
    }
}
