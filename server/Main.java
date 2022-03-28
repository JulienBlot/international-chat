package io.international_chat.server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4321;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server listening on port " + port);
        Room.getRoomsList().put("Default", new Room());

        while (!serverSocket.isClosed()) {
            Client client = new Client(serverSocket.accept());
            client.setRoom("Default");
            Client.getClientList().add(client);
            Room.getRoomsList().get("Default").getClientsInRoom().add(client);
            new Thread(client).start();
        }
    }
}
