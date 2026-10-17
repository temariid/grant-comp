package academy.tochkavhoda.competition.dao;

import academy.tochkavhoda.competition.model.Application;
import academy.tochkavhoda.competition.model.Expert;
import academy.tochkavhoda.competition.model.Participant;
import academy.tochkavhoda.competition.model.UserSession;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DataBase {
    // Participants
    Participant addParticipant(Participant participant);
    Optional<Participant> findParticipantById(int id);
    Optional<Participant> findParticipantByLogin(String login);
    Collection<Participant> getAllParticipants();
    void deleteParticipant(int id);

    // Experts
    Expert addExpert(Expert expert);
    Optional<Expert> findExpertById(int id);
    Optional<Expert> findExpertByLogin(String login);
    Collection<Expert> getAllExperts();
    void deleteExpert(int id);

    // Applications
    Application addApplication(Application application);
    Optional<Application> findApplicationById(int id);
    Collection<Application> getAllApplications();
    void deleteApplication(int id);
    List<Application> findApplicationsByDirections(Collection<String> directions);
    List<Application> findApplicationsByParticipantId(int participantId);

    // Sessions
    void addSession(UserSession session);
    Optional<UserSession> findSessionByToken(String token);
    void removeSession(String token);
    void removeSessionsByUserId(int userId, UserSession.UserType userType);

    // Competition settings
    void setTotalFund(long amount);
    long getTotalFund();
    void setThreshold(double threshold);
    double getThreshold();
    boolean isCompetitionClosed();
    void closeCompetition();
    void openCompetition();

    // Results
    List<Application> getWinningApplications();
    void setWinningApplications(List<Application> winners);

    // ID generation helpers (or internal)
    int nextParticipantId();
    int nextExpertId();
    int nextApplicationId();
}
