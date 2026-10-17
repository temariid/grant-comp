package academy.tochkavhoda.competition.model;

public class UserSession {
    public enum UserType {
        PARTICIPANT, EXPERT
    }

    private String token;
    private int userId;
    private UserType userType;

    public UserSession(String token, int userId, UserType userType) {
        this.token = token;
        this.userId = userId;
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public UserType getUserType() {
        return userType;
    }
}
