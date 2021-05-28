package io.github.donut.proj.databus;

import lombok.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DataBus {
    private final HashMap<String, Set<Member>> hashMap;
//    private final Set<Member> set;

    private DataBus() {
        hashMap = new HashMap<>();
//        set = new HashSet<>();
    }

    private static class InstanceHolder {
        private static final DataBus INSTANCE = new DataBus();
    }

    public static DataBus getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /*=============FOR DEBUGGING============================*/
//    public void printList() {
//        hashMap.forEach((key, value) -> {
//            System.out.println(key);
//            value.forEach((member) -> {
//                System.out.println("- " + ((Main.ClientHandler) member).uuid);
//            });
//            System.out.println();
//        });
//    }
    /*=============FOR DEBUGGING END==========================*/

//    public void logMember(@NonNull Member client) {
//        set.add(client);
//    }
//
//    public void removeMember(@NonNull Member client) {
//        set.remove(client);
//    }

    public void register(@NonNull Member client, @NonNull String... messagesType) {
        for (var message : messagesType) {
            hashMap.computeIfAbsent(message, k -> new HashSet<>()).add(client);
        }
    }

    public void unregister(@NonNull Member client, @NonNull String... messagesType) {
        for (var message : messagesType) {
            Set<?> reference = hashMap.get(message);
            if (reference != null) {
                reference.remove(client);

                if (reference.isEmpty())
                    hashMap.remove(message);
            }
        }
    }

//    public void publishError(@NonNull Member client, @NonNull String json) {
//        removeMember(client);
//        set.forEach((member -> {
//            member.send(json);
//        }));
//    }

    public void publish(@NonNull String type, @NonNull String json) {
        Set<Member> clients = hashMap.get(type);
        if (clients != null) {
            clients.forEach(k -> {
                k.send(json);
            });
        }
    }

    public boolean hasListeners(@NonNull String message) {
        return hashMap.get(message) != null;
    }

    public void cleanup() {
        hashMap.clear();
//        set.clear();
    }
}
