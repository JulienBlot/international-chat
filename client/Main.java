package io.international_chat.client;

import java.io.IOException;
import java.net.Socket;

public class Main {
    private static IOCommandes ioc;

    public static void main(String[] args) throws IOException {
        String host = null;
        int port = 4321;
        if (args.length > 0) {
            host = args[0];
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
        }

        Socket socket = new Socket(host, port);

        ioc = new IOCommandes(socket);

        (new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    String message = ioc.socketRead();
                    ioc.println(message);
                    if (message.equals("Goodbye, see you soon!")) {
                        System.exit(0);
                    }

                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (e.getMessage().equals("Connection reset")) {
                        ioc.println("Connection lost with the server!");
                        System.exit(1);
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        })).start();

        while (!socket.isClosed()) {
            ioc.socketWrite(ioc.readLine());
        }
    }
}
