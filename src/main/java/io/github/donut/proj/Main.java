package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.databus.Member;
import io.github.donut.proj.utils.BufferWrapper;
import io.github.donut.proj.utils.GsonWrapper;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class Main {
    private static  final int MAX_T = 8;

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
        private String[] messages;
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
                while (!isClosed) {
                    JsonObject json = GsonWrapper.fromJson(buffer.readLine());
                    if (json == null) return;

                    switch (json.get("type").getAsString()) {
                    case "Subscribe":
                        messages = GsonWrapper.fromJson(json.get("channels")
                                .getAsJsonArray()
                                .toString(), String[].class);
                        DataBus.register(this, messages);
                        break;
                    case "Message":
                        json.remove("type");
                        DataBus.publish(json.get("channels").getAsString(), json.toString());
                        break;
                    case "PlayerInfo":
                        buffer.writeLine(DBManager.getInstance().getPlayerInfo(json.get("username").getAsString()));
                        isClosed = true;
                        break;
                    case "CreateAccount":
                        JSONObject returnJson = new JSONObject();
                        boolean successful = DBManager.getInstance().createAccount(json.get("firstname").getAsString(),
                                json.get("lastname").getAsString(),
                                json.get("username").getAsString(),
                                json.get("password").getAsString());

                        returnJson.put("isSuccess", successful);
                        buffer.writeLine(returnJson.toString());
                        isClosed = true;
                        break;
                    case "UpdateLastName":
                        JSONObject returnLastNameJson = new JSONObject();
                        boolean lastNameSuccess = DBManager.getInstance().updateLastName(json.get("username").getAsString(),
                                json.get("lastname").getAsString());

                        returnLastNameJson.put("isSuccess", lastNameSuccess);
                        buffer.writeLine(returnLastNameJson.toString());
                        isClosed = true;
                        break;
                    case "UpdateUserName":
                        JSONObject returnUpdatedUsernameJson = new JSONObject();
                        boolean userNameUpdateSuccess = DBManager.getInstance().updateUserName(json.get("oldusername").getAsString(),
                                json.get("newusername").getAsString());

                        returnUpdatedUsernameJson.put("isSuccess", userNameUpdateSuccess);
                        buffer.writeLine(returnUpdatedUsernameJson.toString());
                        isClosed = true;
                        break;
                    }
                }
            } catch (IOException e) {
                // TODO Grant and I (Joey Campbell) get an exception thrown here when we kill the api
                e.printStackTrace();
            } finally {
                System.out.println("Closing client connection...");

                if (messages != null)
                    DataBus.unregister(this, messages);
                try {
                    buffer.close();
                    socket.close();
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
