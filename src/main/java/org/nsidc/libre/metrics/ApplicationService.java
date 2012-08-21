package org.nsidc.libre.metrics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ApplicationService {
	private EntityManagerFactory emf = null;
	private EntityManager entityManager = null;
	public static final String PERSISTENCE_UNIT_NAME = "metrics";

	public ApplicationService() {
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		entityManager = emf.createEntityManager();
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}	
	
}
