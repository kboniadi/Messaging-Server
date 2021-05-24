package io.github.donut.proj;

import com.google.gson.JsonObject;
import io.github.donut.proj.databus.DataBus;
import io.github.donut.proj.databus.Member;
import io.github.donut.proj.utils.GsonWrapper;
import io.github.donut.proj.utils.IOWrapper;
import io.github.donut.proj.utils.Logger;

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

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            isAlive = false;
//            System.out.println("1");
//            try {
//                System.out.println("2");
//                consumer.interrupt();
//                System.out.println("5");
//                consumer.join();
//                System.out.println("3");
////                List<Runnable> temp = pool.shutdownNow();
////                temp.forEach((client) -> {
////
////                });
//                pool.shutdown();
//                System.out.println("hi");
//            } catch (InterruptedException e) {
//                System.out.println("consumer shutdown");
//            }
//            System.out.println("test");
//        }));

        try (var listener = new ServerSocket(9000)) {
            while (isAlive) {
                pool.execute(new ClientHandler(listener.accept()));
            }
        }
    }
    public static class ClientHandler implements Runnable, Member {
        private final Socket socket;
        String[] messages;
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
                if (messages != null) {
                    Logger.log("Cleaning up DataBus...");
                    DataBus.getInstance().unregister(this, messages);
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
