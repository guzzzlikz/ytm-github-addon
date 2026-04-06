import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Song;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiThread extends Thread {
    private String ytmUrl;
    private String ytmToken;
    private String gitToken;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String lastMessage = "";

    public ApiThread(String ytmUrl, String ytmToken, String gitToken) {
        this.ytmUrl = ytmUrl;
        this.ytmToken = ytmToken;
        this.gitToken = gitToken;
    }

    @Override
    public void run() {
        List<String> emojis = new ArrayList<>(List.of(
                "🎶", "🎵", "🎼", "🎧", "🎤", "🎹", "🎸", "🥁",
                "🔥", "💥", "⚡", "✨", "🌟", "💫",
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎",
                "💖", "💕", "💞", "💓", "💗", "💘"
        ));
        while (true) {
            Song song = formSongFromRequest(ytmUrl, ytmToken);
            if (song == null) {
                this.interrupt();
                break;
            }
            String statusMessage = "🎵 Now playing: " + song.getTitle() + " by " + song.getArtist();
            String emoji = emojis.get((int) (Math.random() * emojis.size()));
            if (!statusMessage.equals(lastMessage)) {
                sendSongToGitHub(statusMessage, emoji, gitToken);
            }
            lastMessage = statusMessage;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    private Song formSongFromRequest(String url, String token) {
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
                            this.interrupt();
                            return null;
                        }
                    })
                    .join();
            if (song == null) {
                this.interrupt();
                return null;
            }
        } catch (IOException e) {
            song = new Song();
            song.setTitle("none");
            song.setArtist("none");
            System.out.println("Song title/artist is set to none");
            this.interrupt();
            return null;
        }
        System.out.println("YTMD request succeeded");
        return song;
    }

    private void sendSongToGitHub(String statusMessage, String emoji, String token) {
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
            this.interrupt();
        }
    }
}
