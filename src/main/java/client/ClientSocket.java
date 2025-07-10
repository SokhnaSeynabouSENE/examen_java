package client;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean login(String pseudo) throws IOException {
        String welcome = receive();
        if (welcome == null || !welcome.startsWith("Bienvenue")) return false;
        send(pseudo);
        String answer = receive();
        if (answer != null && answer.startsWith("Inscription réussie")) {
            return true;
        } else {
            System.out.println(answer);
            close();
            return false;
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    public String receive() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            //
        }
    }

}