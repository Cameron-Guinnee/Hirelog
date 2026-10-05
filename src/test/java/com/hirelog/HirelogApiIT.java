package com.hirelog;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hirelog.followup.FollowUpService;

/** End-to-end tests over HTTP against a real PostgreSQL started by Testcontainers. */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "hirelog.security.username=tester",
        "hirelog.security.password=s3cret-pass",
        "hirelog.security.jwt-secret=integration-test-secret-that-is-long-enough",
        "hirelog.scheduling.enabled=false"
})
class HirelogApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    FollowUpService followUps;

    RestClient http;

    @BeforeEach
    void setUp() {
        http = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    @Test
    void apiRequiresAToken() {
        HttpStatusCode status = http.get().uri("/api/companies")
                .exchange((request, response) -> response.getStatusCode());

        assertThat(status.value()).isEqualTo(401);
    }

    @Test
    void wrongPasswordIsRejected() {
        HttpStatusCode status = http.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "tester", "password", "nope"))
                .exchange((request, response) -> response.getStatusCode());

        assertThat(status.value()).isEqualTo(401);
    }

    @Test
    void healthIsPublic() {
        HttpStatusCode status = http.get().uri("/actuator/health")
                .exchange((request, response) -> response.getStatusCode());

        assertThat(status.value()).isEqualTo(200);
    }

    @Test
    void tracksAnApplicationFromCreationToOffer() {
        String auth = "Bearer " + login();

        JsonNode company = post(auth, "/api/companies", Map.of("name", "Initech " + System.nanoTime()));
        long companyId = company.get("id").asLong();

        JsonNode application = post(auth, "/api/applications",
                Map.of("companyId", companyId, "roleTitle", "Platform Engineer", "source", "Referral"));
        long applicationId = application.get("id").asLong();
        assertThat(application.get("status").asText()).isEqualTo("APPLIED");
        LocalDate appliedOn = LocalDate.parse(application.get("appliedOn").asText());

        for (String status : new String[] {"SCREENING", "INTERVIEWING", "OFFER"}) {
            http.patch().uri("/api/applications/{id}/status", applicationId)
                    .header("Authorization", auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity();
        }

        JsonNode history = get(auth, "/api/applications/" + applicationId + "/history");
        assertThat(history).hasSize(4);
        assertThat(history.get(0).get("fromStatus").isNull()).isTrue();
        assertThat(history.get(3).get("fromStatus").asText()).isEqualTo("INTERVIEWING");
        assertThat(history.get(3).get("toStatus").asText()).isEqualTo("OFFER");

        JsonNode stats = get(auth, "/api/stats/weekly?weekOf=" + appliedOn);
        assertThat(stats.get("applicationsSent").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(stats.get("stages")).hasSize(4);
        assertThat(stats.get("stages").get(3).get("stage").asText()).isEqualTo("OFFER");
        assertThat(stats.get("stages").get(3).get("reached").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void schedulerFlagsDueFollowUps() {
        String auth = "Bearer " + login();
        long companyId = post(auth, "/api/companies", Map.of("name", "Globex " + System.nanoTime()))
                .get("id").asLong();
        long applicationId = post(auth, "/api/applications",
                Map.of("companyId", companyId, "roleTitle", "SRE")).get("id").asLong();
        long overdueId = post(auth, "/api/follow-ups", Map.of(
                "applicationId", applicationId,
                "dueOn", LocalDate.now().minusDays(1).toString(),
                "description", "Chase the hiring manager")).get("id").asLong();
        long futureId = post(auth, "/api/follow-ups", Map.of(
                "applicationId", applicationId,
                "dueOn", LocalDate.now().plusDays(7).toString(),
                "description", "Check in after the offsite")).get("id").asLong();

        followUps.flagDue();

        JsonNode due = get(auth, "/api/follow-ups/due");
        assertThat(due.findValuesAsText("id")).contains(String.valueOf(overdueId))
                .doesNotContain(String.valueOf(futureId));

        http.post().uri("/api/follow-ups/{id}/complete", overdueId)
                .header("Authorization", auth)
                .retrieve()
                .toBodilessEntity();
        assertThat(get(auth, "/api/follow-ups/due").findValuesAsText("id"))
                .doesNotContain(String.valueOf(overdueId));
    }

    private String login() {
        JsonNode token = http.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "tester", "password", "s3cret-pass"))
                .retrieve()
                .body(JsonNode.class);
        return token.get("accessToken").asText();
    }

    private JsonNode post(String auth, String uri, Map<String, ?> body) {
        return http.post().uri(uri)
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode get(String auth, String uri) {
        return http.get().uri(uri)
                .header("Authorization", auth)
                .retrieve()
                .body(JsonNode.class);
    }
}
