package server;

import server.dao.JpaUtil;
import server.dao.MembreDao;
import server.dao.MessageDao;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServeurChat {
    private static final int PORT = 1210;
    private static final int MAX_MEMBRES = 7;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final EntityManager em = JpaUtil.getEntityManager();
    private final MembreDao membreDao = new MembreDao(em);
    private final MessageDao messageDao = new MessageDao(em);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                if (clients.size() >= MAX_MEMBRES) {
                    socket.close();
                    System.out.println("Connexion refusée : groupe plein.");
                    continue;
                }
                ClientHandler client = new ClientHandler(socket, this, membreDao, messageDao);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public synchronized List<ClientHandler> getClients() {
        return new ArrayList<>(clients);
    }

    public static void main(String[] args) {
        new ServeurChat().start();
    }
}