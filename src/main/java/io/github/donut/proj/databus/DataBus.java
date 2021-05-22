package io.github.donut.proj.databus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DataBus {
    private final HashMap<String, Set<Member>> list;

    private DataBus() {
        list = new HashMap<>();
    }

    private static class InstanceHolder {
        private static final DataBus INSTANCE = new DataBus();
    }

    public static DataBus getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void printList() {
        list.forEach((key, value) -> {
            System.out.println(key);
            value.forEach((member) -> {
                System.out.println("- " + member);
            });
        });
    }
    public void register(Member client, String... messagesType) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(messagesType);
        for (var message : messagesType) {
            list.computeIfAbsent(message, k -> new HashSet<>()).add(client);
        }
        System.out.println("Register");
        printList();
    }

    public void unregister(Member client, String... messagesType) {
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
        System.out.println("UnRegister");
        printList();
    }

    public void publish(String type, String json) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(json);
        Set<Member> clients = list.get(type);
        if (clients != null)
            clients.forEach(k -> k.send(json));
    }

    public boolean hasListeners(String message) {
        Objects.requireNonNull(message);
        return list.get(message) != null;
    }

    public void cleanup() {
        list.clear();
    }
}
