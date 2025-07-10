package server.dao;

import server.model.Message;
import javax.persistence.EntityManager;
import java.util.List;

public class MessageDao {
    private final EntityManager em;

    public MessageDao(EntityManager em) {
        this.em = em;
    }

    public void add(Message message) {
        em.getTransaction().begin();
        em.persist(message);
        em.getTransaction().commit();
    }

    public Message get(Long id) {
        return em.find(Message.class, id);
    }

    public List<Message> getAll() {
        return em.createQuery("from Message", Message.class).getResultList();
    }

    public List<Message> findLastMessages(int count) {
        return em.createQuery("SELECT m FROM Message m ORDER BY m.dateEnvoi DESC", Message.class)
                .setMaxResults(count)
                .getResultList();
    }

    public void update(Message message) {
        em.getTransaction().begin();
        em.merge(message);
        em.getTransaction().commit();
    }

    public void delete(Message message) {
        em.getTransaction().begin();
        em.remove(em.contains(message) ? message : em.merge(message));
        em.getTransaction().commit();
    }
}