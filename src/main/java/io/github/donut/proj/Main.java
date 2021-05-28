package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.databus.Member;
import io.github.donut.proj.databus.UnexpectedConnectionLost;
import io.github.donut.proj.utils.GsonWrapper;
import io.github.donut.proj.utils.IOWrapper;
import io.github.donut.proj.utils.Logger;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
//    private static  final int MAX_T = 8;
    private static final BlockingQueue<AbstractMap.SimpleImmutableEntry<ClientHandler, JsonObject>> bufferSink = new LinkedBlockingQueue<>();
    static volatile Boolean isAlive = true;

    public static void main(String[] args) {
        try {
            new Main().startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() throws IOException {
        Logger.init("io/github/donut/proj/configs/logging.properties");
        Logger.log("Started server...");

        var pool = Executors.newCachedThreadPool();

        Thread consumer = new Thread(new ClientBufferConsumer(bufferSink));
        consumer.start();

        try (var listener = new ServerSocket(9000)) {
            while (isAlive) {
                pool.execute(new ClientHandler(listener.accept()));
            }
        }
    }
    public static class ClientHandler implements Runnable, Member {
        public String uuid;
        private final Socket socket;
        String[] channels;
        private final IOWrapper buffer;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            buffer = new IOWrapper.Builder()
                    .withWriter(new DataOutputStream(socket.getOutputStream()))
                    .withReader(new DataInputStream(socket.getInputStream()))
                    .build();
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
                while (isAlive) {
                    JsonObject json = GsonWrapper.fromJson(buffer.readLine());
                    if (json == null) return;
                    bufferSink.put(new AbstractMap.SimpleImmutableEntry<>(this, json));
                }
            } catch (IOException | InterruptedException e) {
                Logger.log("Closing client connection...");
            } finally {
//                DataBus.getInstance().removeMember(this);
                if (channels != null && channels.length > 0) {
                    Logger.log("Unexpected termination...");
                    JSONObject json = new JSONObject();
                    json.put("uuid", this.uuid);
                    json.put("channels", "UnexpectedConnectionLost");
                    json.put("message", new JSONObject(new UnexpectedConnectionLost(this.uuid, System.currentTimeMillis(), this.channels)));
                    Logger.log("Cleaning up DataBus...");
                    DataBus.getInstance().unregister(this, channels);
                    DataBus.getInstance().publish("UnexpectedConnectionLost", json.toString());
                }
                try {
                    Logger.log("Cleaning up buffer and socket connections...");
                    buffer.close();
                    socket.close();
                    Logger.log("Closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void send(String json) {
            buffer.writeLine(json);
        }
    }
}
