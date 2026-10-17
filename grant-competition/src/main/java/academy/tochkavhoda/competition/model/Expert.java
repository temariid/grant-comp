package academy.tochkavhoda.competition.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Expert {
    private int id;
    private String firstName;
    private String lastName;
    private Set<String> directions = new HashSet<>();
    private String login;
    private String password;
    private Set<Integer> ratedApplicationIds = new HashSet<>();

    public Expert() {
    }

    public Expert(int id, String firstName, String lastName, Set<String> directions, String login, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.directions = directions != null ? directions : new HashSet<>();
        this.login = login;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getDirections() {
        return directions;
    }

    public void setDirections(Set<String> directions) {
        this.directions = directions;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Integer> getRatedApplicationIds() {
        return ratedApplicationIds;
    }

    public void setRatedApplicationIds(Set<Integer> ratedApplicationIds) {
        this.ratedApplicationIds = ratedApplicationIds;
    }

    public void addRatedApplicationId(int applicationId) {
        this.ratedApplicationIds.add(applicationId);
    }

    public void removeRatedApplicationId(int applicationId) {
        this.ratedApplicationIds.remove(applicationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expert expert = (Expert) o;
        return id == expert.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
