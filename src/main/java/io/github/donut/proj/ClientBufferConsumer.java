package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.utils.GsonWrapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            while (Main.isAlive) {
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
                case "Unsubscribe":
                    String[] msgToRemove = GsonWrapper.fromJson(temp.getValue().get("channels")
                            .getAsJsonArray()
                            .toString(), String[].class);
                    temp.getKey().messages = worker(temp.getKey().messages, msgToRemove).toArray(new String[0]);
                    DataBus.getInstance().unregister(temp.getKey(), msgToRemove);
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<String> worker(String[] messages, String[] msgToRemove) {
        List<String> list1 = Arrays.asList(messages);
        List<String> list2 = Arrays.asList(msgToRemove);

        List<String> union = new ArrayList<>(list1);
        union.addAll(list2);

        List<String> intersection = new ArrayList<>(list1);
        intersection.retainAll(list2);

        union.removeAll(intersection);

        return union;
    }
}