import com.fasterxml.jackson.databind.ObjectMapper;
import entities.AuthResponse;
import entities.Config;
import entities.Song;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

private static Scanner scanner;
private static ObjectMapper objectMapper;
private static String lastMessage = "";
private static Config c;

public static void main(String[] args) throws IOException, InterruptedException {
    scanner = new Scanner(System.in);
    objectMapper = new ObjectMapper();
    List<String> emojis = new ArrayList<>(List.of(
            "🎶", "🎵", "🎼", "🎧", "🎤", "🎹", "🎸", "🥁",
            "🔥", "💥", "⚡", "✨", "🌟", "💫",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎",
            "💖", "💕", "💞", "💓", "💗", "💘"
    ));
    c = loadConfig();
    validateConfig();
    String ytmToken = c.getYtmToken();
    String gitToken = c.getGitToken();
    int port = c.getPort();
    String url = c.getUrl();
    int choice = -1;
    while (choice != 0) {
        System.out.println("1. Check config");
        System.out.println("2. Change YoutubeMusicDesktop token");
        System.out.println("3. Change GitHub token");
        System.out.println("4. Change port");
        System.out.println("5. Change url");
        System.out.println("0. Start");
        choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1 -> {
                System.out.println("YoutubeMusicDesktop token: " + ytmToken);
                System.out.println("GitHub token: " + gitToken);
                System.out.println("Port: " + port);
                System.out.println("URL: " + url);
            }
            case 2 -> {
                System.out.println("old YoutubeMusicDesktop token: " + ytmToken);
                System.out.println("Enter id (any)");
                System.out.print("id: ");
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
                                    System.out.println("YTMD request failed: " + eb.getMessage());
                                    return null;
                                }
                            })
                            .join();
                } catch (IOException et) {
                    throw new RuntimeException(et);
                }
                System.out.println("ytmToken" + token);
                c.setYtmToken(token);
            }
            case 3 -> {
                System.out.println("old GitHub token: " + gitToken);
                System.out.print("new GitHub token: ");
                gitToken = scanner.nextLine();
                c.setGitToken(gitToken);
                objectMapper.writeValue(new File("config.json"), c);
            }
            case 4 -> {
                System.out.println("old Port: " + port);
                System.out.print("new Port: ");
                port = scanner.nextInt();
                scanner.nextLine();
                c.setPort(port);
                objectMapper.writeValue(new File("config.json"), c);
            }
            case 5 -> {
                System.out.println("old URL: " + url);
                System.out.print("new URL: ");
                url = scanner.nextLine();
                c.setUrl(url);
                objectMapper.writeValue(new File("config.json"), c);
            }
        }
    }
    while (true) {
        StringBuilder ytmUrlBuilder = new StringBuilder();
        ytmUrlBuilder.append("http://");
        ytmUrlBuilder.append(url);
        ytmUrlBuilder.append(":");
        ytmUrlBuilder.append(port);
        ytmUrlBuilder.append("/api/v1/song");
        String ytmUrl = ytmUrlBuilder.toString();
        Song song = formSongFromRequest(ytmUrl, ytmToken);
        String statusMessage = "🎵 Now playing: " + song.getTitle() + " by " + song.getArtist();
        String emoji = emojis.get((int) (Math.random() * emojis.size()));
        if (!statusMessage.equals(lastMessage)) {
            sendSongToGitHub(statusMessage, emoji, gitToken);
        }
        lastMessage = statusMessage;
        Thread.sleep(10000);
    }
}
private static Song formSongFromRequest(String url, String token) {
    Song song;
    try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
        song = client.prepareGet(url)
                .addHeader("Authorization", "Bearer " + token)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.getResponseBody(), Song.class);
                    } catch (Exception e) {
                        System.out.println("Reponse returned invalid body");
                        return null;
                    }
                })
                .join();
    } catch (IOException e) {
        song = new Song();
        song.setTitle("none");
        song.setArtist("none");
        System.out.println("Song title/artist is set to none");
    }
    System.out.println("YTMD request succeeded");
    return song;
}
private static void sendSongToGitHub(String statusMessage, String emoji, String token) {
    try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
        String jsonBody = "{ \"query\": \"mutation ($input: ChangeUserStatusInput!) { changeUserStatus(input: $input) { status { message } } }\","
                + "\"variables\": { \"input\": { \"message\": \"" + statusMessage + "\","
                + "\"emoji\": \"" + emoji + "\"," + "\"limitedAvailability\": false } } }";
        Response response = client.preparePost("https://api.github.com/graphql")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .setBody(jsonBody)
                .execute()
                .toCompletableFuture()
                .join();
        System.out.println("Git returned status code: " + response.getStatusCode());
    } catch (Exception e) {
        System.out.println("GitRequest failed: " + e.getMessage());
    }
}
private static Config loadConfig() throws IOException {
    try {
        c = objectMapper.readValue(new File("config.json"), Config.class);
    } catch (Exception e) {
        System.out.println("config.json not found");
        c = new Config();
        System.out.println("GitHub token is missing, please enter it");
        System.out.print("gitToken (check README.MD): ");
        String gitToken = scanner.nextLine();
        c.setGitToken(gitToken);
        System.out.println("URL for YoutubeMusicDesktop is missing, please enter (or press enter and localhost will be selected)");
        String url = scanner.nextLine();
        if (url.isEmpty()) {
            url = "localhost";
        }
        c.setUrl(url);
        System.out.println("Port is missing, please enter it");
        System.out.print("Port (check README.MD): ");
        int port = scanner.nextInt();
        scanner.nextLine();
        c.setPort(port);
        System.out.println("Is auth in YoutubeMusicDesktop enabled?");
        System.out.print("Y/N ");
        String yn = scanner.nextLine().toUpperCase();
        if (yn.equals("Y")) {
            c.setAuthEnabled(true);
            System.out.println("Enter id (any)");
            System.out.print("id: ");
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
                                System.out.println("YTMD request failed: " + e.getMessage());
                                return null;
                            }
                        })
                        .join();
            } catch (IOException et) {
                throw new RuntimeException(e);
            }
            System.out.println("ytmToken: " + token);
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
        System.out.println("GitHub token is missing, please enter it");
        System.out.print("gitToken (check README.MD): ");
        c.setGitToken(scanner.nextLine());
    }
    while (c.getYtmToken().isEmpty() && c.isAuthEnabled()) {
        System.out.println("Auth in YoutubeMusicDesktop is enabled, wanna disable?");
        System.out.print("Y/N ");
        String yn = scanner.nextLine();
        if (yn.equals("N")) {
            c.setAuthEnabled(false);
            break;
        } else if (yn.equals("Y")) {
            int port = c.getPort();
            String url = c.getUrl();
            System.out.println("Enter id (any)");
            System.out.print("id: ");
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
                                System.out.println("YTMD request failed: " + eb.getMessage());
                                return null;
                            }
                        })
                        .join();
            } catch (IOException et) {
                throw new RuntimeException(et);
            }
            System.out.println("ytmToken: " + token);
            c.setYtmToken(token);
        }
    }
    while (c.getPort() == 0) {
        System.out.println("Port is missing, please enter it");
        System.out.print("Port (check README.MD): ");
        c.setPort(scanner.nextInt());
        scanner.nextLine();
    }
    while (c.getUrl().isEmpty()) {
        System.out.println("URL is missing, please enter it (or press enter to continue, localhost will be set as default)");
        String url = scanner.nextLine();
        if (url.isEmpty()) {
            url = "localhost";
        }
        c.setUrl(url);
    }
    objectMapper.writeValue(new File("config.json"), c);
}
