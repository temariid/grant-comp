package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.server.Server;
import academy.tochkavhoda.competition.server.ServerResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestServerBasic {
    private Server server;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        server = new Server();
    }

    @Test
    void testRegisterAndLoginParticipant() {
        String regJson = """
                {
                  "firstName": "Иван",
                  "lastName": "Иванов",
                  "companyName": "ООО Стартап",
                  "login": "ivanov",
                  "password": "pass123!"
                }
                """;
        ServerResponse regResp = server.registerParticipant(regJson);
        assertEquals(200, regResp.getResponseCode());

        String loginJson = """
                {
                  "login": "ivanov",
                  "password": "pass123!"
                }
                """;
        ServerResponse loginResp = server.loginParticipant(loginJson);
        assertEquals(200, loginResp.getResponseCode());
        JsonObject obj = gson.fromJson(loginResp.getResponseData(), JsonObject.class);
        assertTrue(obj.has("token"));
        assertFalse(obj.get("token").getAsString().isEmpty());
    }

    @Test
    void testRegisterDuplicateLogin() {
        String regJson = """
                {
                  "firstName": "Иван",
                  "lastName": "Иванов",
                  "companyName": "ООО Стартап",
                  "login": "ivanov",
                  "password": "pass123!"
                }
                """;
        assertEquals(200, server.registerParticipant(regJson).getResponseCode());
        ServerResponse resp2 = server.registerParticipant(regJson);
        assertEquals(400, resp2.getResponseCode());
        assertTrue(resp2.getResponseData().contains("error"));
    }

    @Test
    void testRegisterExpertAndRate() {
        // Register participant and add application
        String regPart = """
                {
                  "firstName": "Петр",
                  "lastName": "Петров",
                  "companyName": "ООО Инновации",
                  "login": "petrov",
                  "password": "pass456!"
                }
                """;
        assertEquals(200, server.registerParticipant(regPart).getResponseCode());

        String loginPart = """
                {"login":"petrov","password":"pass456!"}
                """;
        ServerResponse loginResp = server.loginParticipant(loginPart);
        String partToken = gson.fromJson(loginResp.getResponseData(), JsonObject.class).get("token").getAsString();

        String appJson = """
                {
                  "title": "Умный город",
                  "description": "Разработка системы управления городом на основе ИИ",
                  "directions": ["информатика", "математика"],
                  "amount": 1000000
                }
                """;
        ServerResponse addAppResp = server.addApplication(partToken, appJson);
        assertEquals(200, addAppResp.getResponseCode());
        int appId = gson.fromJson(addAppResp.getResponseData(), JsonObject.class).get("id").getAsInt();

        // Register expert
        String regExp = """
                {
                  "firstName": "Анна",
                  "lastName": "Сидорова",
                  "directions": ["информатика", "физика"],
                  "login": "sidorova",
                  "password": "exp789!"
                }
                """;
        assertEquals(200, server.registerExpert(regExp).getResponseCode());

        String loginExp = """
                {"login":"sidorova","password":"exp789!"}
                """;
        ServerResponse expLogin = server.loginExpert(loginExp);
        String expToken = gson.fromJson(expLogin.getResponseData(), JsonObject.class).get("token").getAsString();

        // Rate
        String rateJson = String.format("""
                {"applicationId": %d, "score": 5}
                """, appId);
        ServerResponse rateResp = server.rateApplication(expToken, rateJson);
        assertEquals(200, rateResp.getResponseCode());

        // Get rated
        ServerResponse rated = server.getRatedApplications(expToken);
        assertEquals(200, rated.getResponseCode());
        assertTrue(rated.getResponseData().contains("Умный город"));
    }

    @Test
    void testResultsCalculation() {
        // Setup fund and threshold
        server.setTotalFund(null, "{\"amount\": 2500000}");
        server.setThreshold(null, "{\"threshold\": 3.0}");

        // Participant 1
        ServerResponse r1 = server.registerParticipant("""
                {"firstName":"Alex","lastName":"Alexov","companyName":"Company1","login":"part1","password":"pass1!"}
                """);
        assertEquals(200, r1.getResponseCode());
        ServerResponse login1 = server.loginParticipant("{\"login\":\"part1\",\"password\":\"pass1!\"}");
        assertEquals(200, login1.getResponseCode());
        String t1 = gson.fromJson(login1.getResponseData(), JsonObject.class).get("token").getAsString();
        ServerResponse app1Resp = server.addApplication(t1, """
                {"title":"App1","description":"Desc of app one here long enough","directions":["it"],"amount":1000000}
                """);
        assertEquals(200, app1Resp.getResponseCode());
        int app1Id = gson.fromJson(app1Resp.getResponseData(), JsonObject.class).get("id").getAsInt();

        // Participant 2
        assertEquals(200, server.registerParticipant("""
                {"firstName":"Boris","lastName":"Borisov","companyName":"Company2","login":"part2","password":"pass2!"}
                """).getResponseCode());
        ServerResponse login2 = server.loginParticipant("{\"login\":\"part2\",\"password\":\"pass2!\"}");
        assertEquals(200, login2.getResponseCode());
        String t2 = gson.fromJson(login2.getResponseData(), JsonObject.class).get("token").getAsString();
        ServerResponse app2Resp = server.addApplication(t2, """
                {"title":"App2","description":"Desc of app two here long enough","directions":["it"],"amount":1000000}
                """);
        assertEquals(200, app2Resp.getResponseCode());
        int app2Id = gson.fromJson(app2Resp.getResponseData(), JsonObject.class).get("id").getAsInt();

        // Expert
        assertEquals(200, server.registerExpert("""
                {"firstName":"Elena","lastName":"Expert","directions":["it"],"login":"expert1","password":"exp123!"}
                """).getResponseCode());
        ServerResponse expLogin = server.loginExpert("{\"login\":\"expert1\",\"password\":\"exp123!\"}");
        assertEquals(200, expLogin.getResponseCode());
        String et = gson.fromJson(expLogin.getResponseData(), JsonObject.class).get("token").getAsString();

        // Rate app1 = 5, app2 = 4
        assertEquals(200, server.rateApplication(et, String.format("{\"applicationId\":%d,\"score\":5}", app1Id)).getResponseCode());
        assertEquals(200, server.rateApplication(et, String.format("{\"applicationId\":%d,\"score\":4}", app2Id)).getResponseCode());

        // Close and calculate
        ServerResponse results = server.closeAndCalculateResults(null);
        assertEquals(200, results.getResponseCode());
        assertTrue(results.getResponseData().contains("App1"));
    }
}
