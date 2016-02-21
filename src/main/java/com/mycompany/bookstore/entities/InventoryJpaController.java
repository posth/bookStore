/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bookstore.entities;

import com.mycompany.bookstore.entities.exceptions.NonexistentEntityException;
import com.mycompany.bookstore.entities.exceptions.PreexistingEntityException;
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
public class InventoryJpaController implements Serializable {
    
     /**
     * Default Constructor
     */
    public InventoryJpaController() {
        
    }
    
    @Resource
    private UserTransaction utx;
    
    @PersistenceContext
    private EntityManager em;

//    public InventoryJpaController(UserTransaction utx, EntityManagerFactory emf) {
//        this.utx = utx;
//        this.emf = emf;
//    }
//    private UserTransaction utx = null;
//    private EntityManagerFactory emf = null;
//
//    public EntityManager getEntityManager() {
//        return emf.createEntityManager();
//    }

    public void create(Inventory inventory) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (inventory.getReviewsList() == null) {
            inventory.setReviewsList(new ArrayList<Reviews>());
        }
        if (inventory.getDetailsList() == null) {
            inventory.setDetailsList(new ArrayList<Details>());
        }
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            List<Reviews> attachedReviewsList = new ArrayList<Reviews>();
            for (Reviews reviewsListReviewsToAttach : inventory.getReviewsList()) {
                reviewsListReviewsToAttach = em.getReference(reviewsListReviewsToAttach.getClass(), reviewsListReviewsToAttach.getId());
                attachedReviewsList.add(reviewsListReviewsToAttach);
            }
            inventory.setReviewsList(attachedReviewsList);
            List<Details> attachedDetailsList = new ArrayList<Details>();
            for (Details detailsListDetailsToAttach : inventory.getDetailsList()) {
                detailsListDetailsToAttach = em.getReference(detailsListDetailsToAttach.getClass(), detailsListDetailsToAttach.getId());
                attachedDetailsList.add(detailsListDetailsToAttach);
            }
            inventory.setDetailsList(attachedDetailsList);
            em.persist(inventory);
            for (Reviews reviewsListReviews : inventory.getReviewsList()) {
                Inventory oldIsbnOfReviewsListReviews = reviewsListReviews.getIsbn();
                reviewsListReviews.setIsbn(inventory);
                reviewsListReviews = em.merge(reviewsListReviews);
                if (oldIsbnOfReviewsListReviews != null) {
                    oldIsbnOfReviewsListReviews.getReviewsList().remove(reviewsListReviews);
                    oldIsbnOfReviewsListReviews = em.merge(oldIsbnOfReviewsListReviews);
                }
            }
            for (Details detailsListDetails : inventory.getDetailsList()) {
                Inventory oldIsbnOfDetailsListDetails = detailsListDetails.getIsbn();
                detailsListDetails.setIsbn(inventory);
                detailsListDetails = em.merge(detailsListDetails);
                if (oldIsbnOfDetailsListDetails != null) {
                    oldIsbnOfDetailsListDetails.getDetailsList().remove(detailsListDetails);
                    oldIsbnOfDetailsListDetails = em.merge(oldIsbnOfDetailsListDetails);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findInventory(inventory.getIsbn()) != null) {
                throw new PreexistingEntityException("Inventory " + inventory + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Inventory inventory) throws NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Inventory persistentInventory = em.find(Inventory.class, inventory.getIsbn());
            List<Reviews> reviewsListOld = persistentInventory.getReviewsList();
            List<Reviews> reviewsListNew = inventory.getReviewsList();
            List<Details> detailsListOld = persistentInventory.getDetailsList();
            List<Details> detailsListNew = inventory.getDetailsList();
            List<Reviews> attachedReviewsListNew = new ArrayList<Reviews>();
            for (Reviews reviewsListNewReviewsToAttach : reviewsListNew) {
                reviewsListNewReviewsToAttach = em.getReference(reviewsListNewReviewsToAttach.getClass(), reviewsListNewReviewsToAttach.getId());
                attachedReviewsListNew.add(reviewsListNewReviewsToAttach);
            }
            reviewsListNew = attachedReviewsListNew;
            inventory.setReviewsList(reviewsListNew);
            List<Details> attachedDetailsListNew = new ArrayList<Details>();
            for (Details detailsListNewDetailsToAttach : detailsListNew) {
                detailsListNewDetailsToAttach = em.getReference(detailsListNewDetailsToAttach.getClass(), detailsListNewDetailsToAttach.getId());
                attachedDetailsListNew.add(detailsListNewDetailsToAttach);
            }
            detailsListNew = attachedDetailsListNew;
            inventory.setDetailsList(detailsListNew);
            inventory = em.merge(inventory);
            for (Reviews reviewsListOldReviews : reviewsListOld) {
                if (!reviewsListNew.contains(reviewsListOldReviews)) {
                    reviewsListOldReviews.setIsbn(null);
                    reviewsListOldReviews = em.merge(reviewsListOldReviews);
                }
            }
            for (Reviews reviewsListNewReviews : reviewsListNew) {
                if (!reviewsListOld.contains(reviewsListNewReviews)) {
                    Inventory oldIsbnOfReviewsListNewReviews = reviewsListNewReviews.getIsbn();
                    reviewsListNewReviews.setIsbn(inventory);
                    reviewsListNewReviews = em.merge(reviewsListNewReviews);
                    if (oldIsbnOfReviewsListNewReviews != null && !oldIsbnOfReviewsListNewReviews.equals(inventory)) {
                        oldIsbnOfReviewsListNewReviews.getReviewsList().remove(reviewsListNewReviews);
                        oldIsbnOfReviewsListNewReviews = em.merge(oldIsbnOfReviewsListNewReviews);
                    }
                }
            }
            for (Details detailsListOldDetails : detailsListOld) {
                if (!detailsListNew.contains(detailsListOldDetails)) {
                    detailsListOldDetails.setIsbn(null);
                    detailsListOldDetails = em.merge(detailsListOldDetails);
                }
            }
            for (Details detailsListNewDetails : detailsListNew) {
                if (!detailsListOld.contains(detailsListNewDetails)) {
                    Inventory oldIsbnOfDetailsListNewDetails = detailsListNewDetails.getIsbn();
                    detailsListNewDetails.setIsbn(inventory);
                    detailsListNewDetails = em.merge(detailsListNewDetails);
                    if (oldIsbnOfDetailsListNewDetails != null && !oldIsbnOfDetailsListNewDetails.equals(inventory)) {
                        oldIsbnOfDetailsListNewDetails.getDetailsList().remove(detailsListNewDetails);
                        oldIsbnOfDetailsListNewDetails = em.merge(oldIsbnOfDetailsListNewDetails);
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
                String id = inventory.getIsbn();
                if (findInventory(id) == null) {
                    throw new NonexistentEntityException("The inventory with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
        try {
            utx.begin();
//            em = getEntityManager();
            Inventory inventory;
            try {
                inventory = em.getReference(Inventory.class, id);
                inventory.getIsbn();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The inventory with id " + id + " no longer exists.", enfe);
            }
            List<Reviews> reviewsList = inventory.getReviewsList();
            for (Reviews reviewsListReviews : reviewsList) {
                reviewsListReviews.setIsbn(null);
                reviewsListReviews = em.merge(reviewsListReviews);
            }
            List<Details> detailsList = inventory.getDetailsList();
            for (Details detailsListDetails : detailsList) {
                detailsListDetails.setIsbn(null);
                detailsListDetails = em.merge(detailsListDetails);
            }
            em.remove(inventory);
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

    public List<Inventory> findInventoryEntities() {
        return findInventoryEntities(true, -1, -1);
    }

    public List<Inventory> findInventoryEntities(int maxResults, int firstResult) {
        return findInventoryEntities(false, maxResults, firstResult);
    }

    private List<Inventory> findInventoryEntities(boolean all, int maxResults, int firstResult) {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Inventory.class));
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

    public Inventory findInventory(String id) {
//        EntityManager em = getEntityManager();
        try {
            return em.find(Inventory.class, id);
        } finally {
            em.close();
        }
    }

    public int getInventoryCount() {
//        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Inventory> rt = cq.from(Inventory.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
