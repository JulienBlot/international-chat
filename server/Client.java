package io.international_chat.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
    private static final ArrayList<Client> clientList = new ArrayList<>();

    private final Socket socket;
    private final IOCommandes ioc;
    private String username;
    private String language;
    private String room;
    private Boolean active = false;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        ioc = new IOCommandes(socket);
    }

    public static void privateMessage(String sender, String receiver, String message, String language) {
        for (Client client : clientList) {
            if (client.getUsername().equals(receiver)) {
                message = Translate.translate(message, language, client.getLanguage());
                client.getIoc().socketWrite("Private message from " + sender + ": " + message);
                return;
            }
        }
        for (Client client : clientList) {
            if (client.getUsername().equals(sender)) {
                client.getIoc().socketWrite("No user with the name: " + receiver);
                return;
            }
        }
    }

    public static void serverMessageBroadcast(String message) {
        for (Client client : clientList) {
            if (client.isActive()) {
                client.getIoc().socketWrite(message);
            }
        }
    }

    public static void listUsers(Client receiver) {
        for (Client client : clientList) {
            if (client.isActive()) {
                receiver.getIoc().socketWrite(client.username + " [" + client.room + "]");
            }
        }
    }

    public static boolean usernameIsUsed(String username) {
        for (Client client : clientList) {
            if (client.getUsername() != null && username.equals(client.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public static boolean languageIsValid(String language) {
        return language.equals("fr") || language.equals("en") || language.equals("de") || language.equals("it");
    }

    public static ArrayList<Client> getClientList() {
        return clientList;
    }

    @Override
    public void run() {
        ioc.socketWrite("Please enter an username:");
        try {
            boolean usernameValid = false;
            String username;
            do {
                username = ioc.socketRead();
                if (!username.trim().equals("") && !usernameIsUsed(username)) {
                    usernameValid = true;
                } else {
                    ioc.socketWrite("This username is already used! Please choose another username!");
                }
            } while (!usernameValid);
            ioc.socketWrite("Your messages will be translated from your language and incoming messages will be translated to your language.");
            this.username = username;

            ioc.socketWrite("Please specify a language code from this list: fr for French, en for English, de for German, it for Italian:");
            boolean languageValid = false;
            String language;
            do {
                language = ioc.socketRead();
                if (languageIsValid(language)) {
                    languageValid = true;
                } else {
                    ioc.socketWrite("This language code is not valid!");
                }
            } while (!languageValid);
            this.language = language;
            this.active = true;
            serverMessageBroadcast("~~~ " + username + " has joined the server! ~~~");
            ioc.socketWrite("~~~ Welcome " + username + "! Start chatting! If you need help type /help ~~~");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!socket.isClosed()) {
            try {
                String message = ioc.socketRead();
                if (message.startsWith("/")) {
                    commandInterpreter(message);
                } else {
                    Room.getRoomsList().get(room).roomMessageBroadcast(username, message, language);
                }
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (e.getMessage().equals("Connection reset")) {
                    getClientList().remove(this);
                    Room.getRoomsList().get(room).getClientsInRoom().remove(this);
                    serverMessageBroadcast("~~~ " + username + " left the server! ~~~");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    public void commandInterpreter(String message) {
        if (message.startsWith("/pm")) {
            String[] s = message.split(" ", 3);
            if (s.length < 3) {
                return;
            }
            privateMessage(username, s[1], s[2], language);
        } else if (message.startsWith("/users")) {
            ioc.socketWrite("~~~ USERS LIST ~~~");
            listUsers(this);
        } else if (message.startsWith("/rooms")) {
            ioc.socketWrite("~~~ ROOMS LIST ~~~");
            Room.listRooms(this);
        } else if (message.startsWith("/room create")) {
            String[] s = message.split(" ", 3);
            Room.createRoom(s[2]);
            ioc.socketWrite("~~~ The room " + s[2] + " has been created! ~~~");
        } else if (message.startsWith("/room change")) {
            String[] s = message.split(" ", 3);
            boolean roomExist = Room.changeRoom(this, Room.getRoomsList().get(room), Room.getRoomsList().get(s[2]));
            if (roomExist) {
                Room.getRoomsList().get(room).roomServerMessageBroadcast("~~~ " + username + " left the room! ~~~");
                room = s[2];
                ioc.socketWrite("~~~ You are now in the " + s[2] + " room. ~~~");
                Room.getRoomsList().get(room).roomServerMessageBroadcast("~~~ " + username + " has joined the room! ~~~");
            } else {
                ioc.socketWrite("~~~ This room doesn't exist! Type /room create " + s[2] + " to create the room! ~~~");
            }
        } else if (message.startsWith("/room users")) {
            ioc.socketWrite("~~~ ROOM USERS LIST ~~~");
            Room.getRoomsList().get(room).roomListUsers(this);
        } else if (message.startsWith("/leave")) {
            ioc.socketWrite("Goodbye, see you soon!");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (message.startsWith("/help")) {
            ioc.socketWrite("~~~ CHAT HELP ~~~");
            ioc.socketWrite("/pm [username] [message]       send a private message.");
            ioc.socketWrite("/users                         list users on the server.");
            ioc.socketWrite("/rooms                         list rooms.");
            ioc.socketWrite("/room create [name]            create a new room.");
            ioc.socketWrite("/room change [name]            go in the room.");
            ioc.socketWrite("/room users                    users in the room.");
            ioc.socketWrite("/leave                         exit the chat.");
        } else {
            ioc.socketWrite("~~~ Invalid command! Use /help for a list of command. ~~~");
        }
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public IOCommandes getIoc() {
        return ioc;
    }

    public String getUsername() {
        return username;
    }

    public String getLanguage() {
        return language;
    }

    public Boolean isActive() {
        return active;
    }
}
