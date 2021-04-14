package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.databus.Member;
import io.github.donut.proj.databus.data.AccountData;
import io.github.donut.proj.databus.data.IDataType;
import io.github.donut.proj.databus.data.MoveData;
import io.github.donut.proj.databus.data.RegisterData;
import io.github.donut.proj.utils.BufferWrapper;
import io.github.donut.proj.utils.GsonWrapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class Main {
    private static  final int MAX_T = 20;
//    private static final Set<String> names = new HashSet<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Started server...");
        var pool = Executors.newFixedThreadPool(MAX_T);

        try (var listener = new ServerSocket(9000)) {
            while (true) {
                pool.execute(new ClientHandler(listener.accept()));
            }
        }
    }

    public static class ClientHandler implements Runnable, Member {
        private final Socket socket;
        private String name;
        private String[] messages;
        Thread clientThread;
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
            try {
                while (true) {
                    JsonObject json = GsonWrapper.fromJson(buffer.readLine());
                    System.out.println(json);
                    if (json == null) return;

                    switch (json.get("type").getAsString()) {
                    case "RegisterData":
                        DataBus.register(this, GsonWrapper.fromJson(json.get("messages")
                                .getAsJsonArray()
                                .toString(), String[].class));
                        break;
                    case "AccountData":
                        // database calls
                        // .
                        // .
                        // .
                        break;
                    case "MoveData":
                        DataBus.publish("Movedata", MoveData.of(json.toString()));
                        break;
                    }
                }
//                while(true) {
//                    JsonObject json = GsonWrapper.fromJson(buffer.readLine());
//                    System.out.println(json);
//                    if (json == null) return;
//
//                    if (json.get("type").getAsString().equals("Submission")) {
//                        this.name = json.get("name").getAsString();
//                        this.messages = GsonWrapper.fromJson(json.get("messages").getAsJsonArray().toString(), String[].class);
//
//                        synchronized (names) {
//                            if (!name.isBlank() && !names.contains(name)) {
//                                DataBus.register(this, this.messages);
//                                names.add(name);
//                                break;
//                            }
//                        }
//                        buffer.writeLine("{\"type\":\"INVALID\"}");
//                    }
//                }
//                System.out.println("name: " + this.name + " messages: " + Arrays.toString(this.messages));
//
//                buffer.writeLine(GsonWrapper.toJson(AcceptedData.of(this.name)));
//                DataBus.publish("Message", MessageData.of(this.name + " has joined"));
//                while (true) {
//                    JsonObject json = GsonWrapper.fromJson(buffer.readLine());
//                    if (json == null) return;
//                    if (json.get("type").getAsString().equals("Message")) {
//                        if (json.get("message").getAsString().equals("\\quit")) {
//                            break;
//                        }
//                        DataBus.publish("Message", MessageData.of(this.name + ": " + json.get("message").getAsString()));
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Closing client connection...");
//                if (name != null && !name.isBlank()) {
//                    System.out.println(this.name + " is leaving");
//                    names.remove(name);
//                    DataBus.publish("Message", MessageData.of(this.name + " has left"));
//                }
                if (messages != null) {
                    DataBus.unregister(this, messages);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void send(IDataType event) {
            String json = null;
            if (event instanceof MoveData)
                json = GsonWrapper.toJson((MoveData) event);
            else if (event instanceof AccountData)
                json = GsonWrapper.toJson((AccountData) event);
            else if (event instanceof RegisterData)
                json = GsonWrapper.toJson((RegisterData) event);
            buffer.writeLine(json);
        }
    }
}
