package com.reservas.ms_reservas;

import com.jayway.jsonpath.JsonPath;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.TimeSlot;
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

import java.time.LocalDate;
import java.time.LocalTime;
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
class ReservationFlowIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    EntityManager entityManager;
    @Autowired
    TransactionTemplate tx;

    UUID studentId;
    UUID slotId;
    UUID otherSlotId;

    @BeforeEach
    void seed() {
        tx.executeWithoutResult(status -> {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Professor professor = new Professor();
            professor.setEmail("prof-" + suffix + "@uni.edu");
            professor.setPasswordHash("x");
            professor.setFirstName("Ada");
            professor.setLastName("Lovelace");
            professor.setAcademicTitle("PhD");
            entityManager.persist(professor);

            Student student = new Student();
            student.setEmail("st-" + suffix + "@uni.edu");
            student.setPasswordHash("x");
            student.setFirstName("Alan");
            student.setLastName("Turing");
            student.setStudentCode("S" + suffix);
            entityManager.persist(student);
            studentId = student.getId();

            slotId = persistSlot(professor, LocalTime.of(9, 0)).getId();
            otherSlotId = persistSlot(professor, LocalTime.of(11, 0)).getId();
        });
    }

    @Test
    void fullReservationLifecycle() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        UUID methodology = createType("Methodology " + suffix);
        UUID chapter = createType("Chapter " + suffix);

        MvcResult created = mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(slotId, methodology, "intro")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.timeSlot.status").value("BOOKED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andReturn();
        String reservationId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(slotId, chapter, null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TIME_SLOT_UNAVAILABLE"));

        mvc.perform(get("/reservations/search").param("consultationTypeId", methodology.toString()).param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        mvc.perform(post("/reservations/" + reservationId + "/details").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consultationTypeId\":\"" + chapter + "\",\"comment\":\"ch2\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/reservations/" + reservationId + "/details").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consultationTypeId\":\"" + chapter + "\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/reservations/" + reservationId + "/details").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        mvc.perform(put("/reservations/" + reservationId).contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(otherSlotId, chapter, "only chapter")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeSlot.id").value(otherSlotId.toString()))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].consultationType.id").value(chapter.toString()));

        mvc.perform(delete("/reservations/" + reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.timeSlot.status").value("AVAILABLE"));

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(slotId, methodology, null)))
                .andExpect(status().isCreated());
    }

    @Test
    void consultationTypeSoftDelete() throws Exception {
        UUID id = createType("Soft " + UUID.randomUUID().toString().substring(0, 6));

        mvc.perform(delete("/consultation-types/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/consultation-types/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/consultation-types/search").param("active", "false").param("name", "soft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(slotId, id, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INACTIVE_CONSULTATION_TYPE"));
    }

    @Test
    void errorsAreStandardized() throws Exception {
        mvc.perform(get("/reservations/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.path").exists());
        mvc.perform(get("/reservations").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(get("/reservations/not-a-uuid"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/consultation-types").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private UUID createType(String name) throws Exception {
        MvcResult result = mvc.perform(post("/consultation-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"d\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private String reservationBody(UUID slot, UUID type, String comment) {
        String commentJson = comment == null ? "" : ",\"comment\":\"" + comment + "\"";
        return "{\"studentId\":\"" + studentId + "\",\"timeSlotId\":\"" + slot + "\","
                + "\"details\":[{\"consultationTypeId\":\"" + type + "\"" + commentJson + "}]}";
    }

    private TimeSlot persistSlot(Professor professor, LocalTime start) {
        TimeSlot slot = new TimeSlot();
        slot.setProfessor(professor);
        slot.setSlotDate(LocalDate.now().plusDays(30));
        slot.setStartTime(start);
        slot.setEndTime(start.plusHours(1));
        entityManager.persist(slot);
        return slot;
    }
}
