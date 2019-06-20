package wp.chatbot;


import lombok.Data;

@Data
public class ChatMessage {

    private String content;
    private String sender;
    private String createdDateFormat;

    public ChatMessage() {
        this.createdDateFormat = "Just now";
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}