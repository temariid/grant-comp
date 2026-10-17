package academy.tochkavhoda.competition.server;

import academy.tochkavhoda.competition.dao.DataBase;
import academy.tochkavhoda.competition.daoimpl.DataBaseImpl;
import academy.tochkavhoda.competition.service.CompetitionService;
import academy.tochkavhoda.competition.service.ExpertService;
import academy.tochkavhoda.competition.service.ParticipantService;

/**
 * Main server class for the Grant Competition system.
 * All data is stored in memory.
 * Methods accept JSON strings and return ServerResponse.
 */
public class Server {
    private final ParticipantService participantService;
    private final ExpertService expertService;
    private final CompetitionService competitionService;

    public Server() {
        DataBase dataBase = new DataBaseImpl();
        this.participantService = new ParticipantService(dataBase);
        this.expertService = new ExpertService(dataBase);
        this.competitionService = new CompetitionService(dataBase);
    }

    // ===== Participant methods =====

    /**
     * Register a new participant (firm).
     * requestJsonString: {"firstName":"...", "lastName":"...", "companyName":"...", "login":"...", "password":"..."}
     */
    public ServerResponse registerParticipant(String requestJsonString) {
        return participantService.registerParticipant(requestJsonString);
    }

    /**
     * Alias as in TZ example.
     */
    public ServerResponse addParticipant(String token, String requestJsonString) {
        // TZ example has token, but registration usually doesn't need token.
        // We ignore token for registration.
        return registerParticipant(requestJsonString);
    }

    public ServerResponse loginParticipant(String requestJsonString) {
        return participantService.login(requestJsonString);
    }

    public ServerResponse logoutParticipant(String token) {
        return participantService.logout(token);
    }

    public ServerResponse leaveParticipant(String token) {
        return participantService.leave(token);
    }

    public ServerResponse addApplication(String token, String requestJsonString) {
        return participantService.addApplication(token, requestJsonString);
    }

    public ServerResponse cancelApplication(String token, String requestJsonString) {
        return participantService.cancelApplication(token, requestJsonString);
    }

    public ServerResponse getMyApplications(String token) {
        return participantService.getMyApplications(token);
    }

    // ===== Expert methods =====

    public ServerResponse registerExpert(String requestJsonString) {
        return expertService.registerExpert(requestJsonString);
    }

    public ServerResponse loginExpert(String requestJsonString) {
        return expertService.login(requestJsonString);
    }

    public ServerResponse logoutExpert(String token) {
        return expertService.logout(token);
    }

    public ServerResponse leaveExpert(String token) {
        return expertService.leave(token);
    }

    public ServerResponse rateApplication(String token, String requestJsonString) {
        return expertService.rateApplication(token, requestJsonString);
    }

    public ServerResponse deleteRating(String token, String requestJsonString) {
        return expertService.deleteRating(token, requestJsonString);
    }

    public ServerResponse getApplicationsByDirections(String token, String requestJsonString) {
        return expertService.getApplicationsByDirections(token, requestJsonString);
    }

    public ServerResponse getRatedApplications(String token) {
        return expertService.getRatedApplications(token);
    }

    // ===== Common / Competition methods =====

    /**
     * Unified login that tries both participant and expert.
     */
    public ServerResponse login(String requestJsonString) {
        ServerResponse resp = participantService.login(requestJsonString);
        if (resp.getResponseCode() == 200) {
            return resp;
        }
        return expertService.login(requestJsonString);
    }

    public ServerResponse getParticipants(String token, String requestJsonString) {
        return competitionService.getAllParticipants(token, requestJsonString);
    }

    public ServerResponse getAllApplications(String token, String requestJsonString) {
        return competitionService.getAllApplications(token, requestJsonString);
    }

    public ServerResponse setTotalFund(String token, String requestJsonString) {
        return competitionService.setTotalFund(token, requestJsonString);
    }

    public ServerResponse setThreshold(String token, String requestJsonString) {
        return competitionService.setThreshold(token, requestJsonString);
    }

    public ServerResponse closeAndCalculateResults(String token) {
        return competitionService.closeAndCalculateResults(token);
    }

    public ServerResponse getResults(String token) {
        return competitionService.getResults(token);
    }
}
