package io.international_chat.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Room {
    private static final HashMap<String, Room> roomsList = new HashMap<>();

    private final ArrayList<Client> clientsInRoom = new ArrayList<>();

    public ArrayList<Client> getClientsInRoom() {
        return clientsInRoom;
    }

    public static HashMap<String, Room> getRoomsList() {
        return roomsList;
    }

    public static void listRooms(Client receiver) {
        roomsList.forEach((k, v) -> receiver.getIoc().socketWrite(k));
    }

    public void roomMessageBroadcast(String username, String message, String language) {
        for (Client client : clientsInRoom) {
            if (client.isActive() && !username.equals(client.getUsername())) {
                String translatedMessage = Translate.translate(message, language, client.getLanguage());
                client.getIoc().socketWrite(username + ": " + translatedMessage);
            }
        }
    }

    public void roomServerMessageBroadcast(String message) {
        for (Client client : clientsInRoom) {
            client.getIoc().socketWrite(message);
        }
    }

    public void roomListUsers(Client receiver) {
        for (Client client : clientsInRoom) {
            if (client.isActive()) {
                receiver.getIoc().socketWrite(client.getUsername());
            }
        }
    }

    public static void createRoom(String name) {
        roomsList.put(name, new Room());
    }

    public static boolean changeRoom(Client client, Room oldRoom, Room newRoom) {
        if (newRoom == null) {
            return false;
        }
        oldRoom.getClientsInRoom().remove(client);
        newRoom.getClientsInRoom().add(client);
        return true;
    }
}
