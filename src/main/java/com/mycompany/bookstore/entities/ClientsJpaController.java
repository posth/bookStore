/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bookstore.entities;

import com.mycompany.bookstore.entities.exceptions.NonexistentEntityException;
import com.mycompany.bookstore.entities.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

/**
 *
 * @author Marc
 */
@Named
@RequestScoped
public class ClientsJpaController implements Serializable {
    
    @Resource
    private UserTransaction utx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * Default Constructor
     */
    public ClientsJpaController() {
        
    }

//    public ClientsJpaController(UserTransaction utx, EntityManagerFactory emf) {
//        this.utx = utx;
//        this.emf = emf;
//    }
//    private UserTransaction utx = null;
//    private EntityManagerFactory emf = null;
//
//    public EntityManager getEntityManager() {
//        return emf.createEntityManager();
//    }

    public void create(Clients clients) throws RollbackFailureException, Exception {
        if (clients.getInvoicesList() == null) {
            clients.setInvoicesList(new ArrayList<Invoices>());
        }
        if (clients.getReviewsList() == null) {
            clients.setReviewsList(new ArrayList<Reviews>());
        }
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            List<Invoices> attachedInvoicesList = new ArrayList<Invoices>();
            for (Invoices invoicesListInvoicesToAttach : clients.getInvoicesList()) {
                invoicesListInvoicesToAttach = em.getReference(invoicesListInvoicesToAttach.getClass(), invoicesListInvoicesToAttach.getInvoicenum());
                attachedInvoicesList.add(invoicesListInvoicesToAttach);
            }
            clients.setInvoicesList(attachedInvoicesList);
            List<Reviews> attachedReviewsList = new ArrayList<Reviews>();
            for (Reviews reviewsListReviewsToAttach : clients.getReviewsList()) {
                reviewsListReviewsToAttach = em.getReference(reviewsListReviewsToAttach.getClass(), reviewsListReviewsToAttach.getId());
                attachedReviewsList.add(reviewsListReviewsToAttach);
            }
            clients.setReviewsList(attachedReviewsList);
            em.persist(clients);
            for (Invoices invoicesListInvoices : clients.getInvoicesList()) {
                Clients oldClientnumOfInvoicesListInvoices = invoicesListInvoices.getClientnum();
                invoicesListInvoices.setClientnum(clients);
                invoicesListInvoices = em.merge(invoicesListInvoices);
                if (oldClientnumOfInvoicesListInvoices != null) {
                    oldClientnumOfInvoicesListInvoices.getInvoicesList().remove(invoicesListInvoices);
                    oldClientnumOfInvoicesListInvoices = em.merge(oldClientnumOfInvoicesListInvoices);
                }
            }
            for (Reviews reviewsListReviews : clients.getReviewsList()) {
                Clients oldClientnumOfReviewsListReviews = reviewsListReviews.getClientnum();
                reviewsListReviews.setClientnum(clients);
                reviewsListReviews = em.merge(reviewsListReviews);
                if (oldClientnumOfReviewsListReviews != null) {
                    oldClientnumOfReviewsListReviews.getReviewsList().remove(reviewsListReviews);
                    oldClientnumOfReviewsListReviews = em.merge(oldClientnumOfReviewsListReviews);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Clients clients) throws NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Clients persistentClients = em.find(Clients.class, clients.getClientnum());
            List<Invoices> invoicesListOld = persistentClients.getInvoicesList();
            List<Invoices> invoicesListNew = clients.getInvoicesList();
            List<Reviews> reviewsListOld = persistentClients.getReviewsList();
            List<Reviews> reviewsListNew = clients.getReviewsList();
            List<Invoices> attachedInvoicesListNew = new ArrayList<Invoices>();
            for (Invoices invoicesListNewInvoicesToAttach : invoicesListNew) {
                invoicesListNewInvoicesToAttach = em.getReference(invoicesListNewInvoicesToAttach.getClass(), invoicesListNewInvoicesToAttach.getInvoicenum());
                attachedInvoicesListNew.add(invoicesListNewInvoicesToAttach);
            }
            invoicesListNew = attachedInvoicesListNew;
            clients.setInvoicesList(invoicesListNew);
            List<Reviews> attachedReviewsListNew = new ArrayList<Reviews>();
            for (Reviews reviewsListNewReviewsToAttach : reviewsListNew) {
                reviewsListNewReviewsToAttach = em.getReference(reviewsListNewReviewsToAttach.getClass(), reviewsListNewReviewsToAttach.getId());
                attachedReviewsListNew.add(reviewsListNewReviewsToAttach);
            }
            reviewsListNew = attachedReviewsListNew;
            clients.setReviewsList(reviewsListNew);
            clients = em.merge(clients);
            for (Invoices invoicesListOldInvoices : invoicesListOld) {
                if (!invoicesListNew.contains(invoicesListOldInvoices)) {
                    invoicesListOldInvoices.setClientnum(null);
                    invoicesListOldInvoices = em.merge(invoicesListOldInvoices);
                }
            }
            for (Invoices invoicesListNewInvoices : invoicesListNew) {
                if (!invoicesListOld.contains(invoicesListNewInvoices)) {
                    Clients oldClientnumOfInvoicesListNewInvoices = invoicesListNewInvoices.getClientnum();
                    invoicesListNewInvoices.setClientnum(clients);
                    invoicesListNewInvoices = em.merge(invoicesListNewInvoices);
                    if (oldClientnumOfInvoicesListNewInvoices != null && !oldClientnumOfInvoicesListNewInvoices.equals(clients)) {
                        oldClientnumOfInvoicesListNewInvoices.getInvoicesList().remove(invoicesListNewInvoices);
                        oldClientnumOfInvoicesListNewInvoices = em.merge(oldClientnumOfInvoicesListNewInvoices);
                    }
                }
            }
            for (Reviews reviewsListOldReviews : reviewsListOld) {
                if (!reviewsListNew.contains(reviewsListOldReviews)) {
                    reviewsListOldReviews.setClientnum(null);
                    reviewsListOldReviews = em.merge(reviewsListOldReviews);
                }
            }
            for (Reviews reviewsListNewReviews : reviewsListNew) {
                if (!reviewsListOld.contains(reviewsListNewReviews)) {
                    Clients oldClientnumOfReviewsListNewReviews = reviewsListNewReviews.getClientnum();
                    reviewsListNewReviews.setClientnum(clients);
                    reviewsListNewReviews = em.merge(reviewsListNewReviews);
                    if (oldClientnumOfReviewsListNewReviews != null && !oldClientnumOfReviewsListNewReviews.equals(clients)) {
                        oldClientnumOfReviewsListNewReviews.getReviewsList().remove(reviewsListNewReviews);
                        oldClientnumOfReviewsListNewReviews = em.merge(oldClientnumOfReviewsListNewReviews);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = clients.getClientnum();
                if (findClients(id) == null) {
                    throw new NonexistentEntityException("The clients with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Clients clients;
            try {
                clients = em.getReference(Clients.class, id);
                clients.getClientnum();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The clients with id " + id + " no longer exists.", enfe);
            }
            List<Invoices> invoicesList = clients.getInvoicesList();
            for (Invoices invoicesListInvoices : invoicesList) {
                invoicesListInvoices.setClientnum(null);
                invoicesListInvoices = em.merge(invoicesListInvoices);
            }
            List<Reviews> reviewsList = clients.getReviewsList();
            for (Reviews reviewsListReviews : reviewsList) {
                reviewsListReviews.setClientnum(null);
                reviewsListReviews = em.merge(reviewsListReviews);
            }
            em.remove(clients);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Clients> findClientsEntities() {
        return findClientsEntities(true, -1, -1);
    }

    public List<Clients> findClientsEntities(int maxResults, int firstResult) {
        return findClientsEntities(false, maxResults, firstResult);
    }

    private List<Clients> findClientsEntities(boolean all, int maxResults, int firstResult) {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Clients.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Clients findClients(Integer id) {
//        EntityManager em = getEntityManager();
        try {
            return em.find(Clients.class, id);
        } finally {
            em.close();
        }
    }

    public int getClientsCount() {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Clients> rt = cq.from(Clients.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
