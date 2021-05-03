package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.utils.GsonWrapper;

import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;

public class ClientBufferConsumer implements Runnable {
    private final BlockingQueue<AbstractMap.SimpleImmutableEntry<Main.ClientHandler, JsonObject>> bufferSink;

    public ClientBufferConsumer(BlockingQueue<AbstractMap.SimpleImmutableEntry<Main.ClientHandler, JsonObject>> bufferSink) {
        this.bufferSink = bufferSink;
    }
    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            while (true) {
                AbstractMap.SimpleImmutableEntry<Main.ClientHandler, JsonObject> temp = bufferSink.take();
                switch (temp.getValue().get("type").getAsString()) {
                case "Subscribe":
                    temp.getKey().messages = GsonWrapper.fromJson(temp.getValue().get("channels")
                            .getAsJsonArray()
                            .toString(), String[].class);
                    DataBus.getInstance().register(temp.getKey(), temp.getKey().messages);
                    break;
                case "Message":
                    temp.getValue().remove("type");
                    DataBus.getInstance().publish(temp.getValue().get("channels").getAsString(), temp.getValue().toString());
                    break;
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}