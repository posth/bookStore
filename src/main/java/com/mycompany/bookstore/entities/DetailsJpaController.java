/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bookstore.entities;

import com.mycompany.bookstore.entities.exceptions.NonexistentEntityException;
import com.mycompany.bookstore.entities.exceptions.RollbackFailureException;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author Marc
 */
@Named
@RequestScoped
public class DetailsJpaController implements Serializable {
    
    /**
     * Default Constructor
     */
    public DetailsJpaController() {
        
    }
    
    @Resource
    private UserTransaction utx;
    
    @PersistenceContext
    private EntityManager em;
    

//    public DetailsJpaController(UserTransaction utx, EntityManagerFactory emf) {
//        this.utx = utx;
//        this.emf = emf;
//    }
//    private UserTransaction utx = null;
//    private EntityManagerFactory emf = null;
//
//    public EntityManager getEntityManager() {
//        return emf.createEntityManager();
//    }

    public void create(Details details) throws RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Invoices invoicenum = details.getInvoicenum();
            if (invoicenum != null) {
                invoicenum = em.getReference(invoicenum.getClass(), invoicenum.getInvoicenum());
                details.setInvoicenum(invoicenum);
            }
            Inventory isbn = details.getIsbn();
            if (isbn != null) {
                isbn = em.getReference(isbn.getClass(), isbn.getIsbn());
                details.setIsbn(isbn);
            }
            em.persist(details);
            if (invoicenum != null) {
                invoicenum.getDetailsList().add(details);
                invoicenum = em.merge(invoicenum);
            }
            if (isbn != null) {
                isbn.getDetailsList().add(details);
                isbn = em.merge(isbn);
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

    public void edit(Details details) throws NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Details persistentDetails = em.find(Details.class, details.getId());
            Invoices invoicenumOld = persistentDetails.getInvoicenum();
            Invoices invoicenumNew = details.getInvoicenum();
            Inventory isbnOld = persistentDetails.getIsbn();
            Inventory isbnNew = details.getIsbn();
            if (invoicenumNew != null) {
                invoicenumNew = em.getReference(invoicenumNew.getClass(), invoicenumNew.getInvoicenum());
                details.setInvoicenum(invoicenumNew);
            }
            if (isbnNew != null) {
                isbnNew = em.getReference(isbnNew.getClass(), isbnNew.getIsbn());
                details.setIsbn(isbnNew);
            }
            details = em.merge(details);
            if (invoicenumOld != null && !invoicenumOld.equals(invoicenumNew)) {
                invoicenumOld.getDetailsList().remove(details);
                invoicenumOld = em.merge(invoicenumOld);
            }
            if (invoicenumNew != null && !invoicenumNew.equals(invoicenumOld)) {
                invoicenumNew.getDetailsList().add(details);
                invoicenumNew = em.merge(invoicenumNew);
            }
            if (isbnOld != null && !isbnOld.equals(isbnNew)) {
                isbnOld.getDetailsList().remove(details);
                isbnOld = em.merge(isbnOld);
            }
            if (isbnNew != null && !isbnNew.equals(isbnOld)) {
                isbnNew.getDetailsList().add(details);
                isbnNew = em.merge(isbnNew);
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
                Integer id = details.getId();
                if (findDetails(id) == null) {
                    throw new NonexistentEntityException("The details with id " + id + " no longer exists.");
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
            Details details;
            try {
                details = em.getReference(Details.class, id);
                details.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The details with id " + id + " no longer exists.", enfe);
            }
            Invoices invoicenum = details.getInvoicenum();
            if (invoicenum != null) {
                invoicenum.getDetailsList().remove(details);
                invoicenum = em.merge(invoicenum);
            }
            Inventory isbn = details.getIsbn();
            if (isbn != null) {
                isbn.getDetailsList().remove(details);
                isbn = em.merge(isbn);
            }
            em.remove(details);
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

    public List<Details> findDetailsEntities() {
        return findDetailsEntities(true, -1, -1);
    }

    public List<Details> findDetailsEntities(int maxResults, int firstResult) {
        return findDetailsEntities(false, maxResults, firstResult);
    }

    private List<Details> findDetailsEntities(boolean all, int maxResults, int firstResult) {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Details.class));
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

    public Details findDetails(Integer id) {
//        EntityManager em = getEntityManager();
        try {
            return em.find(Details.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetailsCount() {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Details> rt = cq.from(Details.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
