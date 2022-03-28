package io.international_chat.guiclient;

import java.io.*;
import java.net.Socket;

public class IOCommandes {
    private final BufferedReader commandRead;
    private PrintStream commandWrite;

    private final BufferedReader socketRead;
    private final PrintStream socketWrite;

    public IOCommandes(Socket socket) throws IOException {
        InputStreamReader commandInputStreamReader = new InputStreamReader(System.in);
        commandRead = new BufferedReader(commandInputStreamReader);
        commandWrite = System.out;

        InputStreamReader socketInputStreamReader = new InputStreamReader(socket.getInputStream());
        socketRead = new BufferedReader(socketInputStreamReader);

        OutputStream socketOutputStream = socket.getOutputStream();
        socketWrite = new PrintStream(socketOutputStream);
    }

    public String readLine() throws IOException {
        return commandRead.readLine();
    }

    public void println(String text) {
        commandWrite.println(text);
    }

    public String socketRead() throws IOException {
        return socketRead.readLine();
    }

    public void socketWrite(String text) {
        socketWrite.println(text);
    }

    public void setCommandWrite(PrintStream commandWrite) {
        this.commandWrite = commandWrite;
    }
}
