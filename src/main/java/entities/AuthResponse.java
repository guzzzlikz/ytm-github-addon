package entities;

public class AuthResponse {
    private String accessToken;
    public AuthResponse() {
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
