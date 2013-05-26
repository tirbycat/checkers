package com.mycompany.checkersserver;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.ejb.Ejb3Configuration;

/**
 *
 * @author tirbycat
 */
public class HibernateSessionManager {
//    private static Cache<String, Session> sessions = CacheBuilder.newBuilder()
//    .concurrencyLevel(4)
//    .weakKeys()
//    .maximumSize(10000)
//    .expireAfterWrite(30, TimeUnit.MINUTES)
//    .removalListener(new RemovalListener<String, Session>(){
//        @Override
//        public void onRemoval(RemovalNotification<String, Session> rn) {
//            rn.getValue().flush();
//            rn.getValue().close();;
//        }
//    })
//    .build();
    
    private static SessionFactory sessionFactory;
    private static final ThreadLocal threadSession = new ThreadLocal();
    
    
    private final static Ejb3Configuration cfg = new Ejb3Configuration();
    private final static EntityManagerFactory entityManagerFactory = cfg.configure("hibernate.cfg.xml").buildEntityManagerFactory();
    
    private final static EntityManager entityManager = entityManagerFactory.createEntityManager();
    
    public static void init(){
        try {
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (HibernateException ex) {
            throw new RuntimeException("Configuration problem: " + ex.getMessage(), ex);
        }
    }
    
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
    
    public static Session openSession(){
        Session s = sessionFactory.openSession();
        threadSession.set(s);
        return s;
    }
    
    public static void closeSession(){
        Session session = (Session)threadSession.get();
        if(session != null){
            session.close();
            threadSession.remove();
        }
    }
    
//    public static Session getSession(String id){
//        Session session = sessions.getIfPresent(id);
//        if(session == null){
//            session = sessionFactory.openSession();
//            
//            sessions.put(id, session);
//        }
//        session.reconnect();
//        threadSession.set(session);
//        return session;
//    }
    
    public static Session getUserSession(){
        return (Session)threadSession.get();
    }
    
//    public static void stopSession(String id){
//        Session session = sessions.getIfPresent(id);
//        if(session != null){
//            session.disconnect();
//        }
//    }
    
//    public static void closeSession(String id){
//        Session session = sessions.getIfPresent(id);
//        if(session != null){
//            session.flush();
//            session.close();
//            sessions.invalidate(id);
//        }
//    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }
}
