package server;

import server.dao.MembreDao;
import server.dao.MessageDao;
import server.model.Membre;
import server.model.Message;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServeurChat serveur;
    private final MembreDao membreDao;
    private final MessageDao messageDao;
    private Membre membre;
    private BufferedReader in;
    private PrintWriter out;
    private static final List<String> BLACKLIST = Arrays.asList("GENOCID", "TERRORISM", "ATTACK", "CHELSEA", "JAVA NEKHOUL");

    public ClientHandler(Socket socket, ServeurChat serveur, MembreDao membreDao, MessageDao messageDao) {
        this.socket = socket;
        this.serveur = serveur;
        this.membreDao = membreDao;
        this.messageDao = messageDao;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Bienvenue ! Entrez votre pseudo :");
            String pseudo = in.readLine();

            if (pseudo == null || pseudo.trim().isEmpty() || membreDao.findByPseudo(pseudo) != null) {
                out.println("Pseudo invalide ou déjà utilisé.");
                out.flush();
                close();
                return;
            }
            if (membreDao.countNonBanned() >= 7) {
                out.println("Groupe plein");
                out.flush();
                close();
                return;
            }

            membre = new Membre();
            membre.setPseudo(pseudo);
            membre.setBanned(false);
            membreDao.add(membre);

            out.println("Inscription réussie.");
            out.flush();

            // Envoi des 15 derniers messages (du plus ancien au plus récent)
            List<Message> lastMessages = messageDao.findLastMessages(15);
            for (int i = lastMessages.size() - 1; i >= 0; i--) {
                Message msg = lastMessages.get(i);
                out.println("[" + msg.getDateEnvoi() + "] " + msg.getMembre().getPseudo() + ": " + msg.getContenu());
            }

            // Boucle principale de chat
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("quit")) {
                    out.println("Vous avez quitté le groupe.");
                    out.flush();
                    membreDao.delete(membre);

                    // *** Message système envoyé à tous les autres membres ***
                    broadcast("[Serveur] " + membre.getPseudo() + " a quitté le groupe.", this);

                    break;
                }
                if (containsBlacklistedWord(line)) {
                    membre.setBanned(true);
                    membreDao.update(membre);
                    out.println("Vous êtes banni pour message inapproprié.");
                    out.flush();
                    broadcast("[Serveur] " + membre.getPseudo() + " a été banni.", this);
                    break;
                }
                Message message = new Message();
                message.setMembre(membre);
                message.setContenu(line);
                message.setDateEnvoi(LocalDateTime.now());
                messageDao.add(message);
                broadcast("[" + message.getDateEnvoi() + "] " + membre.getPseudo() + ": " + line, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serveur.removeClient(this); // Retire le client du serveur AVANT fermeture
            close(); // Ferme les flux et la socket: plus aucun message ne sera traité/émis
        }
    }


    private boolean containsBlacklistedWord(String text) {
        String upper = text.toUpperCase();
        return BLACKLIST.stream().anyMatch(upper::contains);
    }

    public String getPseudo() {
        return membre != null ? membre.getPseudo() : null;
    }

    /**
     * Diffuse un message à tous les clients sauf 'exclude'.
     */
    private void broadcast(String message, ClientHandler exclude) {
        String excludePseudo = exclude != null ? exclude.getPseudo() : null;
        for (ClientHandler client : serveur.getClients()) {
            if (client.getPseudo() == null || !client.getPseudo().equals(excludePseudo)) {
                client.sendMessage(message);
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    private void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // rien
        }
    }
}