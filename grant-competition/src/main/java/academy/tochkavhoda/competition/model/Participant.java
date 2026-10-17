package academy.tochkavhoda.competition.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Participant {
    private int id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String login;
    private String password;
    private List<Integer> applicationIds = new ArrayList<>();

    public Participant() {
    }

    public Participant(int id, String firstName, String lastName, String companyName, String login, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public List<Integer> getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(List<Integer> applicationIds) {
        this.applicationIds = applicationIds;
    }

    public void addApplicationId(int applicationId) {
        this.applicationIds.add(applicationId);
    }

    public void removeApplicationId(int applicationId) {
        this.applicationIds.remove(Integer.valueOf(applicationId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
