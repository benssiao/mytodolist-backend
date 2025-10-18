package com.mytodolist.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mytodolist.security.dtos.RegisterRequestDTO;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class EntryFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void setupEach() throws Exception {
        RegisterRequestDTO register = new RegisterRequestDTO("testuser" + System.currentTimeMillis(), "Password1");
        String testUserandPassword = objectMapper.writeValueAsString(register);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUserandPassword))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUserandPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(post("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryBody\": \"This is a test entry.\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetEntries_Returns200() throws Exception {

        mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entryBody").value("This is a test entry."));

    }

    @Test
    void testGetEntries_UnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/entries")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateEntry_Returns201() throws Exception {
        mockMvc.perform(post("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryBody\": \"Another test entry.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entryBody").value("Another test entry."));

        mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateEntry_Returns200() throws Exception {

        MvcResult entriesResult = mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        Integer entryId = JsonPath.read(entriesResult.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(put("/api/v1/entries/" + entryId)
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryBody\": \"Updated test entry.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryBody").value("Updated test entry."));

        MvcResult updatedEntry = mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String updatedEntryBody = JsonPath.read(updatedEntry.getResponse().getContentAsString(), "$[0].entryBody");

        assertThat(updatedEntryBody.equals("Updated test entry."));
    }

    @Test
    void testUpdateEntry_AlterOtherUser_Returns403() throws Exception {
        // Register and login as a different user
        RegisterRequestDTO otherUser = new RegisterRequestDTO("otheruser" + System.currentTimeMillis(), "Password1");
        String otherUserJson = objectMapper.writeValueAsString(otherUser);

        mockMvc.perform(post("/api/v1/auth/register") // register user2
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(otherUserJson))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(otherUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String otherUserToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        MvcResult entriesResult = mockMvc.perform(get("/api/v1/entries") // get user1s entries
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        Integer entryId = JsonPath.read(entriesResult.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(put("/api/v1/entries/" + entryId) // user2 tries to update user1s entry
                .with(csrf())
                .header("Authorization", "Bearer " + otherUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryBody\": \"Malicious update attempt.\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteEntry_Returns204() throws Exception {

        MvcResult entriesResult = mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        Integer entryId = JsonPath.read(entriesResult.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/entries/" + entryId)
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testDeleteEntry_AlterOtherUser_Returns403() throws Exception {
        // Register and login as a different user
        RegisterRequestDTO otherUser = new RegisterRequestDTO("otheruser" + System.currentTimeMillis(), "Password1");
        String otherUserJson = objectMapper.writeValueAsString(otherUser);

        mockMvc.perform(post("/api/v1/auth/register") // register user2
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(otherUserJson))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(otherUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String otherUserToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        MvcResult entriesResult = mockMvc.perform(get("/api/v1/entries") // get user1s entries
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        Integer entryId = JsonPath.read(entriesResult.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/entries/" + entryId) // user2 tries to delete user1s entry
                .with(csrf())
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

}
