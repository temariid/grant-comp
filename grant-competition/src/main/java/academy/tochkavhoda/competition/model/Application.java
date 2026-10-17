package academy.tochkavhoda.competition.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Application {
    private int id;
    private String title;
    private String description;
    private Set<String> directions = new HashSet<>();
    private long amount; // requested grant amount
    private int participantId;
    private Map<Integer, Integer> ratings = new HashMap<>(); // expertId -> score (1-5)

    public Application() {
    }

    public Application(int id, String title, String description, Set<String> directions, long amount, int participantId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.directions = directions != null ? directions : new HashSet<>();
        this.amount = amount;
        this.participantId = participantId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getDirections() {
        return directions;
    }

    public void setDirections(Set<String> directions) {
        this.directions = directions;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Map<Integer, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<Integer, Integer> ratings) {
        this.ratings = ratings;
    }

    public void addOrUpdateRating(int expertId, int score) {
        this.ratings.put(expertId, score);
    }

    public void removeRating(int expertId) {
        this.ratings.remove(expertId);
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
