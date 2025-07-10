package server.dao;

import server.model.Membre;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public class MembreDao {
    private final EntityManager em;

    public MembreDao(EntityManager em) {
        this.em = em;
    }

    public void add(Membre membre) {
        em.getTransaction().begin();
        em.persist(membre);
        em.getTransaction().commit();
    }

    public Membre get(Long id) {
        return em.find(Membre.class, id);
    }

    public List<Membre> getAll() {
        return em.createQuery("from Membre", Membre.class).getResultList();
    }

    public Membre findByPseudo(String pseudo) {
        try {
            return em.createQuery("SELECT m FROM Membre m WHERE m.pseudo = :pseudo", Membre.class)
                    .setParameter("pseudo", pseudo)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(Membre membre) {
        em.getTransaction().begin();
        em.merge(membre);
        em.getTransaction().commit();
    }

    public void delete(Membre membre) {
        em.getTransaction().begin();
        em.remove(em.contains(membre) ? membre : em.merge(membre));
        em.getTransaction().commit();
    }

    public long countNonBanned() {
        return em.createQuery("SELECT COUNT(m) FROM Membre m WHERE m.banned = false", Long.class)
                .getSingleResult();
    }
}