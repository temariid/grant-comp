package academy.tochkavhoda.competition.daoimpl;

import academy.tochkavhoda.competition.dao.DataBase;
import academy.tochkavhoda.competition.model.Application;
import academy.tochkavhoda.competition.model.Expert;
import academy.tochkavhoda.competition.model.Participant;
import academy.tochkavhoda.competition.model.UserSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DataBaseImpl implements DataBase {
    private final Map<Integer, Participant> participants = new ConcurrentHashMap<>();
    private final Map<Integer, Expert> experts = new ConcurrentHashMap<>();
    private final Map<Integer, Application> applications = new ConcurrentHashMap<>();
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> loginToParticipantId = new ConcurrentHashMap<>();
    private final Map<String, Integer> loginToExpertId = new ConcurrentHashMap<>();

    private final AtomicInteger participantIdGenerator = new AtomicInteger(1);
    private final AtomicInteger expertIdGenerator = new AtomicInteger(1);
    private final AtomicInteger applicationIdGenerator = new AtomicInteger(1);

    private long totalFund = 0;
    private double threshold = 0.0;
    private boolean competitionClosed = false;
    private List<Application> winningApplications = new ArrayList<>();

    @Override
    public Participant addParticipant(Participant participant) {
        int id = nextParticipantId();
        participant.setId(id);
        participants.put(id, participant);
        loginToParticipantId.put(participant.getLogin(), id);
        return participant;
    }

    @Override
    public Optional<Participant> findParticipantById(int id) {
        return Optional.ofNullable(participants.get(id));
    }

    @Override
    public Optional<Participant> findParticipantByLogin(String login) {
        Integer id = loginToParticipantId.get(login);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(participants.get(id));
    }

    @Override
    public Collection<Participant> getAllParticipants() {
        return new ArrayList<>(participants.values());
    }

    @Override
    public void deleteParticipant(int id) {
        Participant p = participants.remove(id);
        if (p != null) {
            loginToParticipantId.remove(p.getLogin());
            // remove applications
            for (Integer appId : new ArrayList<>(p.getApplicationIds())) {
                deleteApplication(appId);
            }
            removeSessionsByUserId(id, UserSession.UserType.PARTICIPANT);
        }
    }

    @Override
    public Expert addExpert(Expert expert) {
        int id = nextExpertId();
        expert.setId(id);
        experts.put(id, expert);
        loginToExpertId.put(expert.getLogin(), id);
        return expert;
    }

    @Override
    public Optional<Expert> findExpertById(int id) {
        return Optional.ofNullable(experts.get(id));
    }

    @Override
    public Optional<Expert> findExpertByLogin(String login) {
        Integer id = loginToExpertId.get(login);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(experts.get(id));
    }

    @Override
    public Collection<Expert> getAllExperts() {
        return new ArrayList<>(experts.values());
    }

    @Override
    public void deleteExpert(int id) {
        Expert e = experts.remove(id);
        if (e != null) {
            loginToExpertId.remove(e.getLogin());
            // remove ratings from applications
            for (Application app : applications.values()) {
                app.removeRating(id);
            }
            removeSessionsByUserId(id, UserSession.UserType.EXPERT);
        }
    }

    @Override
    public Application addApplication(Application application) {
        int id = nextApplicationId();
        application.setId(id);
        applications.put(id, application);
        // link to participant
        findParticipantById(application.getParticipantId()).ifPresent(p -> p.addApplicationId(id));
        return application;
    }

    @Override
    public Optional<Application> findApplicationById(int id) {
        return Optional.ofNullable(applications.get(id));
    }

    @Override
    public Collection<Application> getAllApplications() {
        return new ArrayList<>(applications.values());
    }

    @Override
    public void deleteApplication(int id) {
        Application app = applications.remove(id);
        if (app != null) {
            findParticipantById(app.getParticipantId()).ifPresent(p -> p.removeApplicationId(id));
            // remove from experts' rated lists
            for (Integer expertId : app.getRatings().keySet()) {
                findExpertById(expertId).ifPresent(e -> e.removeRatedApplicationId(id));
            }
        }
    }

    @Override
    public List<Application> findApplicationsByDirections(Collection<String> directions) {
        if (directions == null || directions.isEmpty()) {
            return new ArrayList<>(applications.values());
        }
        Set<String> dirSet = directions.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return applications.values().stream()
                .filter(app -> app.getDirections().stream().map(String::toLowerCase).anyMatch(dirSet::contains))
                .collect(Collectors.toList());
    }

    @Override
    public List<Application> findApplicationsByParticipantId(int participantId) {
        return applications.values().stream()
                .filter(app -> app.getParticipantId() == participantId)
                .collect(Collectors.toList());
    }

    @Override
    public void addSession(UserSession session) {
        sessions.put(session.getToken(), session);
    }

    @Override
    public Optional<UserSession> findSessionByToken(String token) {
        return Optional.ofNullable(sessions.get(token));
    }

    @Override
    public void removeSession(String token) {
        sessions.remove(token);
    }

    @Override
    public void removeSessionsByUserId(int userId, UserSession.UserType userType) {
        sessions.entrySet().removeIf(e -> e.getValue().getUserId() == userId && e.getValue().getUserType() == userType);
    }

    @Override
    public void setTotalFund(long amount) {
        this.totalFund = amount;
    }

    @Override
    public long getTotalFund() {
        return totalFund;
    }

    @Override
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public double getThreshold() {
        return threshold;
    }

    @Override
    public boolean isCompetitionClosed() {
        return competitionClosed;
    }

    @Override
    public void closeCompetition() {
        this.competitionClosed = true;
    }

    @Override
    public void openCompetition() {
        this.competitionClosed = false;
        this.winningApplications = new ArrayList<>();
    }

    @Override
    public List<Application> getWinningApplications() {
        return new ArrayList<>(winningApplications);
    }

    @Override
    public void setWinningApplications(List<Application> winners) {
        this.winningApplications = new ArrayList<>(winners);
    }

    @Override
    public int nextParticipantId() {
        return participantIdGenerator.getAndIncrement();
    }

    @Override
    public int nextExpertId() {
        return expertIdGenerator.getAndIncrement();
    }

    @Override
    public int nextApplicationId() {
        return applicationIdGenerator.getAndIncrement();
    }
}
