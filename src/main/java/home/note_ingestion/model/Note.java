package home.note_ingestion.model;

public class Note {

    private String user;
    private String topic;
    private String text;

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}