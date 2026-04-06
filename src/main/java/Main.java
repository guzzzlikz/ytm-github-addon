import com.fasterxml.jackson.databind.ObjectMapper;
import entities.AuthResponse;
import entities.Config;
import entities.Song;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    private static Date date;
    private static Scanner scanner;
    private static ObjectMapper objectMapper;
    private static Config c;
    private static ApiThread apiThread;
    private static final Logger logger = new Logger();
    public static void main(String[] args) throws IOException, InterruptedException {
        scanner = new Scanner(System.in);
        objectMapper = new ObjectMapper();
        c = loadConfig();
        validateConfig();
        launchMenu();
        while (true) {
            if (!apiThread.isAlive()) {
                apiThread.join();
                launchMenu();
            }
        }
    }

    private static Config loadConfig() throws IOException {
        try {
            c = objectMapper.readValue(new File("config.json"), Config.class);
        } catch (Exception e) {
            logger.log("config.json not found");
            c = new Config();
            logger.log("GitHub token is missing, please enter it");
            logger.log("gitToken (check README.MD): ");
            String gitToken = scanner.nextLine();
            c.setGitToken(gitToken);
            logger.logln("URL for YoutubeMusicDesktop is missing, please enter (or press enter and localhost will be selected)");
            String url = scanner.nextLine();
            if (url.isEmpty()) {
                url = "localhost";
            }
            c.setUrl(url);
            logger.logln("Port is missing, please enter it");
            logger.log("Port (check README.MD): ");
            int port = scanner.nextInt();
            scanner.nextLine();
            c.setPort(port);
            logger.logln("Is auth in YoutubeMusicDesktop enabled?");
            logger.log("Y/N ");
            String yn = scanner.nextLine().toUpperCase();
            if (yn.equals("Y")) {
                c.setAuthEnabled(true);
                logger.logln("Enter id (any)");
                logger.log("id: ");
                String id = scanner.nextLine();
                String token;
                try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
                    token = client.preparePost("http://" + url + ":" + port + "/auth/" + id)
                            .addHeader("Content-Type", "application/json")
                            .execute()
                            .toCompletableFuture()
                            .thenApply(response -> {
                                try {
                                    AuthResponse auth = objectMapper.readValue(response.getResponseBody(), AuthResponse.class);
                                    return auth.getAccessToken(); // extract the token
                                } catch (Exception eb) {
                                    logger.logln("YTMD request failed: " + e.getMessage());
                                    return null;
                                }
                            })
                            .join();
                } catch (IOException et) {
                    throw new RuntimeException(e);
                }
                logger.logln("ytmToken: " + token);
                c.setYtmToken(token);
            } else {
                c.setAuthEnabled(false);
            }
            objectMapper.writeValue(new File("config.json"), c);
        }
        return c;
    }

    private static void validateConfig() throws IOException {
        while (c.getGitToken().isEmpty()) {
            logger.logln("GitHub token is missing, please enter it");
            logger.log("gitToken (check README.MD): ");
            c.setGitToken(scanner.nextLine());
        }
        while (c.getYtmToken().isEmpty() && c.isAuthEnabled()) {
            logger.logln("Auth in YoutubeMusicDesktop is enabled, wanna disable?");
            logger.log("Y/N ");
            String yn = scanner.nextLine();
            if (yn.equals("N")) {
                c.setAuthEnabled(false);
                break;
            } else if (yn.equals("Y")) {
                int port = c.getPort();
                String url = c.getUrl();
                logger.logln("Enter id (any)");
                logger.log("id: ");
                String id = scanner.nextLine();
                String token;
                try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
                    token = client.preparePost("http://" + url + ":" + port + "/auth/" + id)
                            .addHeader("Content-Type", "application/json")
                            .execute()
                            .toCompletableFuture()
                            .thenApply(response -> {
                                try {
                                    AuthResponse auth = objectMapper.readValue(response.getResponseBody(), AuthResponse.class);
                                    return auth.getAccessToken();
                                } catch (Exception eb) {
                                    logger.logln("YTMD request failed: " + eb.getMessage());
                                    return null;
                                }
                            })
                            .join();
                } catch (IOException et) {
                    throw new RuntimeException(et);
                }
                logger.logln("ytmToken: " + token);
                c.setYtmToken(token);
            }
        }
        while (c.getPort() == 0) {
            logger.logln("Port is missing, please enter it");
            logger.log("Port (check README.MD): ");
            c.setPort(scanner.nextInt());
            scanner.nextLine();
        }
        while (c.getUrl().isEmpty()) {
            logger.logln("URL is missing, please enter it (or press enter to continue, localhost will be set as default)");
            String url = scanner.nextLine();
            if (url.isEmpty()) {
                url = "localhost";
            }
            c.setUrl(url);
        }
        objectMapper.writeValue(new File("config.json"), c);
    }

    private static void launchMenu() throws IOException, InterruptedException {
        String ytmToken = c.getYtmToken();
        String gitToken = c.getGitToken();
        int port = c.getPort();
        String url = c.getUrl();
        int choice = -1;
        while (choice != 0) {
            logger.logln("1. Check config");
            logger.logln("2. Change YoutubeMusicDesktop token");
            logger.logln("3. Change GitHub token");
            logger.logln("4. Change port");
            logger.logln("5. Change url");
            logger.logln("0. Start");
            logger.log("");
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> {
                    logger.logln("YoutubeMusicDesktop token: " + ytmToken);
                    logger.logln("GitHub token: " + gitToken);
                    logger.logln("Port: " + port);
                    logger.logln("URL: " + url);
                }
                case 2 -> {
                    logger.logln("old YoutubeMusicDesktop token: " + ytmToken);
                    logger.logln("Enter id (any)");
                    logger.log("id: ");
                    String id = scanner.nextLine();
                    String token;
                    try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
                        token = client.preparePost("http://" + url + ":" + port + "/auth/" + id)
                                .addHeader("Content-Type", "application/json")
                                .execute()
                                .toCompletableFuture()
                                .thenApply(response -> {
                                    try {
                                        AuthResponse auth = objectMapper.readValue(response.getResponseBody(), AuthResponse.class);
                                        return auth.getAccessToken();
                                    } catch (Exception eb) {
                                        logger.logln("YTMD request failed: " + eb.getMessage());
                                        return null;
                                    }
                                })
                                .join();
                    } catch (IOException et) {
                        throw new RuntimeException(et);
                    }
                    logger.logln("ytmToken" + token);
                    c.setYtmToken(token);
                    objectMapper.writeValue(new File("config.json"), c);
                }
                case 3 -> {
                    logger.logln("old GitHub token: " + gitToken);
                    logger.log("new GitHub token: ");
                    gitToken = scanner.nextLine();
                    c.setGitToken(gitToken);
                    objectMapper.writeValue(new File("config.json"), c);
                }
                case 4 -> {
                    logger.logln("old Port: " + port);
                    logger.log("new Port: ");
                    port = scanner.nextInt();
                    scanner.nextLine();
                    c.setPort(port);
                    objectMapper.writeValue(new File("config.json"), c);
                }
                case 5 -> {
                    logger.logln("old URL: " + url);
                    logger.log("new URL: ");
                    url = scanner.nextLine();
                    c.setUrl(url);
                    objectMapper.writeValue(new File("config.json"), c);
                }
            }
        }
        ytmToken = c.getYtmToken();
        gitToken = c.getGitToken();
        port = c.getPort();
        url = c.getUrl();
        StringBuilder ytmUrlBuilder = new StringBuilder();
        ytmUrlBuilder.append("http://");
        ytmUrlBuilder.append(url);
        ytmUrlBuilder.append(":");
        ytmUrlBuilder.append(port);
        ytmUrlBuilder.append("/api/v1/song");
        String ytmUrl = ytmUrlBuilder.toString();
        apiThread = new ApiThread(ytmUrl, ytmToken, gitToken);
        apiThread.start();
        Thread.sleep(500);
    }
}