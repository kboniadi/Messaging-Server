package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.databus.Member;
import io.github.donut.proj.utils.BufferWrapper;
import io.github.donut.proj.utils.GsonWrapper;
import io.github.donut.proj.utils.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class Main {
    private static  final int MAX_T = 8;
    private static final BlockingQueue<AbstractMap.SimpleImmutableEntry<ClientHandler, JsonObject>> bufferSink = new ArrayBlockingQueue<>(MAX_T);

    public static void main(String[] args) throws IOException {
        Logger.init("io/github/donut/proj/configs/logging.properties");
        Logger.log("Started server...");

        var pool = Executors.newFixedThreadPool(MAX_T);

        new Thread(new ClientBufferConsumer(bufferSink)).start();

        try (var listener = new ServerSocket(9000)) {
            while (true) {
                pool.execute(new ClientHandler(listener.accept()));
            }
        }
    }

    public static class ClientHandler implements Runnable, Member {
        private final Socket socket;
        String[] messages;
        BufferWrapper buffer;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            buffer = new BufferWrapper.Builder()
                    .withWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)))
                    .withReader(new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
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
            boolean isClosed = false;
            try {
                while (true) {
                    JsonObject json = GsonWrapper.fromJson(buffer.readLine());
                    if (json == null) return;

                    bufferSink.put(new AbstractMap.SimpleImmutableEntry<>(this, json));
//                    switch (json.get("type").getAsString()) {
//                    case "Subscribe":
//                    case "Message":
//                        bufferSink.put(new AbstractMap.SimpleImmutableEntry<>(this, json));
//                        break;
//                    case "PlayerInfo":
//                        buffer.writeLine(DBManager.getInstance().getPlayerInfo(json.get("username").getAsString()));
//                        isClosed = true;
//                        break;
//                    case "CreateAccount":
//                        JSONObject returnJson = new JSONObject();
//                        boolean successful = DBManager.getInstance().createAccount(
//                                json.get("firstname").getAsString(),
//                                json.get("lastname").getAsString(),
//                                json.get("username").getAsString(),
//                                json.get("password").getAsString());
//
//                        returnJson.put("isSuccess", successful);
//                        buffer.writeLine(returnJson.toString());
//                        isClosed = true;
//                        break;
//                    }
                }
            } catch (IOException | InterruptedException e) {
                // TODO Grant and I (Joey Campbell) get an exception thrown here when we kill the api
                e.printStackTrace();
            } finally {
                Logger.log("Closing client connection...");

                if (messages != null) {
                    Logger.log("Cleaning up DataBus...");
                    DataBus.unregister(this, messages);
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
