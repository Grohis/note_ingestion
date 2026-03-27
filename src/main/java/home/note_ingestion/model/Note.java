package home.note_ingestion.model;

import java.util.List;

public class Note {

    private String user;
    private String topic;
    private String text;
    private String title;
    private String content;
    private List<String> tags;

    public List<String> getTags() {return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getContent() {return user; }
    public void setContent(String user) {this.user = content; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}