package tn.learniverse.controllers;

public class YouTubeVideo {
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String channelTitle;

    public YouTubeVideo(String id, String title, String description, String thumbnailUrl, String channelTitle) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getChannelTitle() { return channelTitle; }
}