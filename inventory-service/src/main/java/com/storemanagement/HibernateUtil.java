package com.storemanagement;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
    try {
        Configuration cfg = new Configuration().configure("hibernate.cfg.xml");

        // Optional overrides from environment variables (used by Docker).
        // If a variable is not set, the values in hibernate.cfg.xml apply as before.
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        if (url != null && !url.isEmpty()) cfg.setProperty("hibernate.connection.url", url);
        if (user != null && !user.isEmpty()) cfg.setProperty("hibernate.connection.username", user);
        if (pass != null) cfg.setProperty("hibernate.connection.password", pass);

        return cfg.buildSessionFactory();
        } catch (Throwable ex) {
        System.err.println("Initial SessionFactory creation failed." + ex);
        throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}