package com.reservas.ms_notificaciones;

import com.jayway.jsonpath.JsonPath;
import com.reservas.commons.entity.Student;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationFlowIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    EntityManager entityManager;
    @Autowired
    TransactionTemplate tx;

    UUID userId;

    @BeforeEach
    void seedUser() {
        tx.executeWithoutResult(status -> {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Student student = new Student();
            student.setEmail("st-" + suffix + "@uni.edu");
            student.setPasswordHash("x");
            student.setFirstName("Alan");
            student.setLastName("Turing");
            student.setStudentCode("S" + suffix);
            entityManager.persist(student);
            userId = student.getId();
        });
    }

    @Test
    void templateLifecycleAndSoftDelete() throws Exception {
        String code = "TPL_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String body = "{\"code\":\"" + code + "\",\"subject\":\"Hi {{name}}\",\"htmlBody\":\"<p>{{name}}</p>\"}";

        MvcResult created = mvc.perform(post("/email-templates").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/email-templates").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL_TEMPLATE"));

        mvc.perform(put("/email-templates/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"subject\":\"Updated\",\"htmlBody\":\"<p>x</p>\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Updated"));

        mvc.perform(get("/email-templates/search").param("text", code.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));

        mvc.perform(delete("/email-templates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/email-templates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/email-templates").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(1));
    }

    @Test
    void sendNotificationUsesTemplateOfItsType() throws Exception {
        mvc.perform(post("/email-templates").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"REMINDER\",\"subject\":\"Reminder {{date}}\",\"htmlBody\":\"<p>{{date}}</p>\"}"));

        mvc.perform(post("/notifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + userId + "\",\"type\":\"REMINDER\",\"payload\":{\"date\":\"2026-11-01\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.payload.date").value("2026-11-01"));

        mvc.perform(get("/notifications/search").param("userId", userId.toString()).param("status", "SENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));

        mvc.perform(post("/notifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + UUID.randomUUID() + "\",\"type\":\"REMINDER\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(post("/notifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"REMINDER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void contactFormIsStoredAndErrorsAreStandardized() throws Exception {
        mvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@mail.com\",\"message\":\"Help <b>please</b>\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        mvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"not-an-email\",\"message\":\"x\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/contact/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/contact").param("size", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
