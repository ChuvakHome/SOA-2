package ru.itmo.se.soa.lab2.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {
	private static final String PERSISTENT_UNIT_NAME = "ru.itmo.se.soa";
    
//	private static SessionFactory factory;
	
    private static EntityManagerFactory entityManagerFactory;

    private HibernateUtil() {}

//    public static synchronized SessionFactory getSessionFactory() {
//        if (factory == null) {
//            factory = new Configuration()
//                    .configure("hibernate.cfg.xml")
//                    .addAnnotatedClass(VehicleEntity.class)
//                    .buildSessionFactory();
//        }
//        return factory;
//    }
    
    public static EntityManager getEntityManager() {
    	if (entityManagerFactory == null) {
    		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENT_UNIT_NAME);
//    		entityManagerFactory = new HibernatePersistenceProvider().createEntityManagerFactory(PERSISTENT_UNIT_NAME, null);
    	}
    	
    	return entityManagerFactory.createEntityManager();
    }
}
