package com.storemanagement;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/items")
public class ItemServlet extends HttpServlet {

    private final Gson gson = new Gson();

    // GET: Retrieve all items (Open to Admin and Employee)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        
        // 1. Verify Session exists
        HttpSession httpSession = req.getSession(false); // 'false' means don't create a new one if it doesn't exist
        if (httpSession == null || httpSession.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Please log in first.\"}");
            return;
        }

        // 2. Fetch items using Hibernate
        try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {
            List<Item> items = dbSession.createQuery("FROM Item", Item.class).list();
            
            // Convert list to JSON and send
            String jsonOutput = gson.toJson(items);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonOutput);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    // POST: Add a new item (STRICTLY ADMIN ONLY)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        // 1. Security Check: Are you an Admin?
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || !"ADMIN".equals(httpSession.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403 Blocked!
            out.write("{\"error\": \"Access Denied. Admins only.\"}");
            return;
        }

        // 2. Parse the incoming JSON data using Gson
        try {
            Item newItem = gson.fromJson(req.getReader(), Item.class);
            // Generate ID for the new item since the client won't send one
            newItem.setId("itm_" + java.util.UUID.randomUUID().toString().substring(0, 8)); 

            // 3. Save to database
            try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = dbSession.beginTransaction();
                dbSession.persist(newItem);
                tx.commit();

                resp.setStatus(HttpServletResponse.SC_CREATED); // HTTP 201 Created
                out.write(gson.toJson(newItem));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Invalid item data format.\"}");
            e.printStackTrace();
        }
    }

    // ==========================================
    // DELETE: Remove an item from the database
    // ==========================================
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. RBAC Security Check: Only ADMIN can delete
        HttpSession session = req.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 2. Get the item ID from the URL (e.g., /api/items?id=123)
        String itemId = req.getParameter("id");
        if (itemId == null || itemId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 3. Delete from PostgreSQL using Hibernate
        try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = dbSession.beginTransaction();
            Item item = dbSession.get(Item.class, itemId);
            
            if (item != null) {
                dbSession.remove(item);
                tx.commit();
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204: Successfully processed, no content to return
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // PUT: Update an existing item in the database
    // ==========================================
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. RBAC Security Check: Only ADMIN can update
        HttpSession session = req.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 2. Parse the incoming JSON body
        Item updatedData = gson.fromJson(req.getReader(), Item.class);

        // 3. Update PostgreSQL using Hibernate
        try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = dbSession.beginTransaction();
            Item existingItem = dbSession.get(Item.class, updatedData.getId());
            
            if (existingItem != null) {
                // Update the fields
                existingItem.setName(updatedData.getName());
                existingItem.setPrice(updatedData.getPrice());
                existingItem.setStockQuantity(updatedData.getStockQuantity());
                
                dbSession.merge(existingItem);
                tx.commit();
                
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(existingItem));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}