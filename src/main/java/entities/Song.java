package entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Song {
    private String title;
    private String alternativeTitle;
    private String artist;
    private String artistUrl;
    private int views;
    private String uploadDate;
    private String imageSrc;
    private boolean isPaused;
    private int songDuration;
    private int elapsedSeconds;
    private String url;
    private String album;
    private String videoId;
    private String playlistId;
    private String mediaType;
    private String[] tags;

    // Default constructor
    public Song() {}

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAlternativeTitle() { return alternativeTitle; }
    public void setAlternativeTitle(String alternativeTitle) { this.alternativeTitle = alternativeTitle; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getArtistUrl() { return artistUrl; }
    public void setArtistUrl(String artistUrl) { this.artistUrl = artistUrl; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public String getImageSrc() { return imageSrc; }
    public void setImageSrc(String imageSrc) { this.imageSrc = imageSrc; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }

    public int getSongDuration() { return songDuration; }
    public void setSongDuration(int songDuration) { this.songDuration = songDuration; }

    public int getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(int elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getPlaylistId() { return playlistId; }
    public void setPlaylistId(String playlistId) { this.playlistId = playlistId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
}