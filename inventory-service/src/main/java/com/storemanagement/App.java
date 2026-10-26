package com.storemanagement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting Hibernate...");

        // 1. Build the SessionFactory (reads hibernate.cfg.xml and connects to DB)
        SessionFactory factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();

        // 2. Open a session and start a transaction
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            // Create some inventory items
            Item laptop = new Item("ThinkPad T14", 1200.00, 10);
            Item mouse = new Item("Logitech Wireless Mouse", 45.00, 50);
            Item keyboard = new Item("Mechanical Keyboard", 85.00, 20);

            // Save them to the database
            session.persist(laptop);
            session.persist(mouse);
            session.persist(keyboard);

            // Commit the transaction
            tx.commit();
            System.out.println("Inventory items successfully seeded!");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}