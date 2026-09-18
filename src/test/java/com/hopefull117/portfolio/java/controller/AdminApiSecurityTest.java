package com.hopefull117.portfolio.java.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiSecurityTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void rejectsAnonymousAdminApiAccessWithUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminSessionWithCsrfProtectedWrites() throws Exception {
        mockMvc.perform(get("/api/admin/summary"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/technologies")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Java\",\"iconUrl\":null}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsStateChangingAdminRequestsWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/admin/technologies")
                        .contentType("application/json")
                        .content("{\"name\":\"Java\",\"iconUrl\":null}"))
                .andExpect(status().isForbidden());
    }
}
