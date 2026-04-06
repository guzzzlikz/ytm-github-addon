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
    private final Logger logger = new Logger();
    private int emojiId;
    private static final List<String> emojis = new ArrayList<>(List.of(
            "🎶", "🎵", "🎼", "🎧", "🎤", "🎹", "🎸", "🥁",
            "🔥", "💥", "⚡", "✨", "🌟", "💫",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎",
            "💖", "💕", "💞", "💓", "💗", "💘", "🕊️"
    ));

    public ApiThread(String ytmUrl, String ytmToken, String gitToken, int emojiId) {
        this.ytmUrl = ytmUrl;
        this.ytmToken = ytmToken;
        this.gitToken = gitToken;
        this.emojiId = emojiId;
    }

    @Override
    public void run() {
        while (true) {
            Song song = formSongFromRequest(ytmUrl, ytmToken);
            if (song == null) {
                this.interrupt();
                break;
            }
            String statusMessage = "🎵 Now playing: " + song.getTitle() + " by " + song.getArtist();
            String emoji;
            if (emojiId == -1) {
                emoji = emojis.get((int) (Math.random() * emojis.size()));
            } else {
                emoji = emojis.get(emojiId);
            }
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
                            logger.logln("Reponse returned invalid body");
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
            logger.logln("Song title/artist is set to none");
            this.interrupt();
            return null;
        }
        logger.logln("YTMD request succeeded");
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
            logger.logln("Git returned status code: " + response.getStatusCode());
        } catch (Exception e) {
            logger.logln("GitRequest failed: " + e.getMessage());
            this.interrupt();
        }
    }
}
