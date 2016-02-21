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
public class InvoicesJpaController implements Serializable {
    
     /**
     * Default Constructor
     */
    public InvoicesJpaController() {
        
    }
    
    @Resource
    private UserTransaction utx;
    
    @PersistenceContext
    private EntityManager em;
    

//    public InvoicesJpaController(UserTransaction utx, EntityManagerFactory emf) {
//        this.utx = utx;
//        this.emf = emf;
//    }
//    private UserTransaction utx = null;
//    private EntityManagerFactory emf = null;
//
//    public EntityManager getEntityManager() {
//        return emf.createEntityManager();
//    }

    public void create(Invoices invoices) throws RollbackFailureException, Exception {
        if (invoices.getDetailsList() == null) {
            invoices.setDetailsList(new ArrayList<Details>());
        }
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Clients clientnum = invoices.getClientnum();
            if (clientnum != null) {
                clientnum = em.getReference(clientnum.getClass(), clientnum.getClientnum());
                invoices.setClientnum(clientnum);
            }
            List<Details> attachedDetailsList = new ArrayList<Details>();
            for (Details detailsListDetailsToAttach : invoices.getDetailsList()) {
                detailsListDetailsToAttach = em.getReference(detailsListDetailsToAttach.getClass(), detailsListDetailsToAttach.getId());
                attachedDetailsList.add(detailsListDetailsToAttach);
            }
            invoices.setDetailsList(attachedDetailsList);
            em.persist(invoices);
            if (clientnum != null) {
                clientnum.getInvoicesList().add(invoices);
                clientnum = em.merge(clientnum);
            }
            for (Details detailsListDetails : invoices.getDetailsList()) {
                Invoices oldInvoicenumOfDetailsListDetails = detailsListDetails.getInvoicenum();
                detailsListDetails.setInvoicenum(invoices);
                detailsListDetails = em.merge(detailsListDetails);
                if (oldInvoicenumOfDetailsListDetails != null) {
                    oldInvoicenumOfDetailsListDetails.getDetailsList().remove(detailsListDetails);
                    oldInvoicenumOfDetailsListDetails = em.merge(oldInvoicenumOfDetailsListDetails);
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

    public void edit(Invoices invoices) throws NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Invoices persistentInvoices = em.find(Invoices.class, invoices.getInvoicenum());
            Clients clientnumOld = persistentInvoices.getClientnum();
            Clients clientnumNew = invoices.getClientnum();
            List<Details> detailsListOld = persistentInvoices.getDetailsList();
            List<Details> detailsListNew = invoices.getDetailsList();
            if (clientnumNew != null) {
                clientnumNew = em.getReference(clientnumNew.getClass(), clientnumNew.getClientnum());
                invoices.setClientnum(clientnumNew);
            }
            List<Details> attachedDetailsListNew = new ArrayList<Details>();
            for (Details detailsListNewDetailsToAttach : detailsListNew) {
                detailsListNewDetailsToAttach = em.getReference(detailsListNewDetailsToAttach.getClass(), detailsListNewDetailsToAttach.getId());
                attachedDetailsListNew.add(detailsListNewDetailsToAttach);
            }
            detailsListNew = attachedDetailsListNew;
            invoices.setDetailsList(detailsListNew);
            invoices = em.merge(invoices);
            if (clientnumOld != null && !clientnumOld.equals(clientnumNew)) {
                clientnumOld.getInvoicesList().remove(invoices);
                clientnumOld = em.merge(clientnumOld);
            }
            if (clientnumNew != null && !clientnumNew.equals(clientnumOld)) {
                clientnumNew.getInvoicesList().add(invoices);
                clientnumNew = em.merge(clientnumNew);
            }
            for (Details detailsListOldDetails : detailsListOld) {
                if (!detailsListNew.contains(detailsListOldDetails)) {
                    detailsListOldDetails.setInvoicenum(null);
                    detailsListOldDetails = em.merge(detailsListOldDetails);
                }
            }
            for (Details detailsListNewDetails : detailsListNew) {
                if (!detailsListOld.contains(detailsListNewDetails)) {
                    Invoices oldInvoicenumOfDetailsListNewDetails = detailsListNewDetails.getInvoicenum();
                    detailsListNewDetails.setInvoicenum(invoices);
                    detailsListNewDetails = em.merge(detailsListNewDetails);
                    if (oldInvoicenumOfDetailsListNewDetails != null && !oldInvoicenumOfDetailsListNewDetails.equals(invoices)) {
                        oldInvoicenumOfDetailsListNewDetails.getDetailsList().remove(detailsListNewDetails);
                        oldInvoicenumOfDetailsListNewDetails = em.merge(oldInvoicenumOfDetailsListNewDetails);
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
                Integer id = invoices.getInvoicenum();
                if (findInvoices(id) == null) {
                    throw new NonexistentEntityException("The invoices with id " + id + " no longer exists.");
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
            Invoices invoices;
            try {
                invoices = em.getReference(Invoices.class, id);
                invoices.getInvoicenum();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The invoices with id " + id + " no longer exists.", enfe);
            }
            Clients clientnum = invoices.getClientnum();
            if (clientnum != null) {
                clientnum.getInvoicesList().remove(invoices);
                clientnum = em.merge(clientnum);
            }
            List<Details> detailsList = invoices.getDetailsList();
            for (Details detailsListDetails : detailsList) {
                detailsListDetails.setInvoicenum(null);
                detailsListDetails = em.merge(detailsListDetails);
            }
            em.remove(invoices);
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

    public List<Invoices> findInvoicesEntities() {
        return findInvoicesEntities(true, -1, -1);
    }

    public List<Invoices> findInvoicesEntities(int maxResults, int firstResult) {
        return findInvoicesEntities(false, maxResults, firstResult);
    }

    private List<Invoices> findInvoicesEntities(boolean all, int maxResults, int firstResult) {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Invoices.class));
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

    public Invoices findInvoices(Integer id) {
//        EntityManager em = getEntityManager();
        try {
            return em.find(Invoices.class, id);
        } finally {
            em.close();
        }
    }

    public int getInvoicesCount() {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Invoices> rt = cq.from(Invoices.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
