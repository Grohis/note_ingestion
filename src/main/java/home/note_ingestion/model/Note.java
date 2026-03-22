package home.note_ingestion.model;

public class Note {

    private String user;
    private String topic;
    private String text;
    private String title;

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}