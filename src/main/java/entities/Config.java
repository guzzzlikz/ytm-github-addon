package entities;

public class Config {
    private String ytmToken;
    private String gitToken;
    private String url;
    private int port;
    private boolean authEnabled;
    public Config() {}

    public String getYtmToken() {
        return ytmToken;
    }

    public void setYtmToken(String ytmToken) {
        this.ytmToken = ytmToken;
    }

    public String getGitToken() {
        return gitToken;
    }

    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }
}
