package com.springshop.springshop.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
abstract class ApiTestSupport {
    @Value("${app.admin.email}")
    protected String adminEmail;

    @Value("${app.admin.password}")
    protected String adminPassword;
    static final String USER_PASSWORD = "motdepasse123";

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "test@springboot.com";
    }

    protected String adminToken() throws Exception {
        return login(adminEmail, adminPassword);
    }

    protected String newUserToken() throws Exception {
        return registerUser(uniqueEmail());
    }

    protected String registerUser(String email) throws Exception {
        String body = """
                {"fullName":"Test User", "email":"%s", "password":"%s"}
                """.formatted(email, USER_PASSWORD);
        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON)).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    protected String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s", "password":"%s"}
                """.formatted(email, password);
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON)).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    protected long createCategory(String adminToken, String name) throws Exception {
        String response = mockMvc.perform(post("/api/categories")
                .header("Authorization", bearer(adminToken))
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    protected long createProduct(String adminToken, String name, String price, int stock, Long categoryId) throws Exception {
        String body = """
                {"name":"%s", "description":"Description de test","price":%s, "stock":%d,"categoryId":%s}
                """.formatted(name, price, stock, categoryId == null ? "null" : categoryId.toString());
        String response = mockMvc.perform(post("/api/products")
                .header("Authorization", bearer(adminToken))
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
