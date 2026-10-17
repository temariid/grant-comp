package academy.tochkavhoda.competition.server;

public class ServerResponse {
    private int responseCode;
    private String responseData;

    public ServerResponse() {
    }

    public ServerResponse(int responseCode, String responseData) {
        this.responseCode = responseCode;
        this.responseData = responseData;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public static ServerResponse success(String responseData) {
        return new ServerResponse(200, responseData != null ? responseData : "{}");
    }

    public static ServerResponse success() {
        return new ServerResponse(200, "{}");
    }

    public static ServerResponse error(String errorMessage) {
        return new ServerResponse(400, "{\"error\":\"" + escapeJson(errorMessage) + "\"}");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
