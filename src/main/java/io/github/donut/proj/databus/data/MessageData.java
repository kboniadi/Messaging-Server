package io.github.donut.proj.databus.data;

public class MessageData extends AbstractDataType {
    public final String type = "MessageData";
    public final String message;

    private MessageData(String message) {
        this.message = message;
    }

    public static IDataType of(String message) {
        return new MessageData(message);
    }
}
