package io.github.donut.proj.databus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DataBus {
    private static final HashMap<String, Set<Member>> list = new HashMap<>();

    private DataBus() {
        // empty
    }

//    private static class InstanceHolder {
//        private static final DataBus INSTANCE = new DataBus();
//    }
//
//    public static DataBus getInstance() {
//        return InstanceHolder.INSTANCE;
//    }

    public static void register(Member client, String... messagesType) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(messagesType);
        for (var message : messagesType) {
            list.computeIfAbsent(message, k -> new HashSet<>()).add(client);
        }
    }

    public static void unregister(Member client, String... messagesType) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(messagesType);
        for (var message : messagesType) {
            Set<?> reference = list.get(message);
            if (reference != null) {
                reference.remove(client);

                if (reference.isEmpty())
                    list.remove(message);
            }
        }
    }

    public static void publish(String type, String json) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(json);
        Set<Member> clients = list.get(type);
        if (clients != null)
            clients.forEach(k -> k.send(json));
    }

    public static boolean hasListeners(String message) {
        Objects.requireNonNull(message);
        return list.get(message) != null;
    }

    public static void cleanup() {
        list.clear();
    }
}
