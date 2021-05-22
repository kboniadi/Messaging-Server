package io.github.donut.proj.databus;

import lombok.NonNull;

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

    /*=============FOR DEBUGGING============================*/
//    public void printList() {
//        list.forEach((key, value) -> {
//            System.out.println(key);
//            value.forEach((member) -> {
//                System.out.println("- " + member);
//            });
//        });
//    }
    /*=============FOR DEBUGGING END==========================*/

    public void register(@NonNull Member client, @NonNull String... messagesType) {
        for (var message : messagesType) {
            list.computeIfAbsent(message, k -> new HashSet<>()).add(client);
        }
    }

    public void unregister(@NonNull Member client, @NonNull String... messagesType) {
        for (var message : messagesType) {
            Set<?> reference = list.get(message);
            if (reference != null) {
                reference.remove(client);

                if (reference.isEmpty())
                    list.remove(message);
            }
        }
    }

    public void publish(@NonNull String type, @NonNull String json) {
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
