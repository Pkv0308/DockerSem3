package com.storemanagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;

@WebServlet("/api/items/decrement")
public class DecrementStockServlet extends HttpServlet {

    private final Gson gson = new Gson();

    // A simple helper class for Gson to map the incoming JSON
    public static class DecrementRequest {
        public String itemId;
        public int quantity;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            // 1. Parse the incoming JSON array into a List of DecrementRequest objects
            Type listType = new TypeToken<List<DecrementRequest>>(){}.getType();
            List<DecrementRequest> requestedItems = gson.fromJson(req.getReader(), listType);

            // 2. Open DB Session and start a Transaction
            try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = dbSession.beginTransaction();

                // 3. Loop through the requested items
                for (DecrementRequest reqItem : requestedItems) {
                    Item dbItem = dbSession.get(Item.class, reqItem.itemId);

                    // Error: Item doesn't exist
                    if (dbItem == null) {
                        tx.rollback();
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"message\": \"Item ID not found: " + reqItem.itemId + "\"}");
                        return;
                    }

                    // Error: Not enough stock
                    if (dbItem.getStockQuantity() < reqItem.quantity) {
                        tx.rollback();
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"message\": \"Insufficient stock for: " + dbItem.getName() + "\"}");
                        return;
                    }

                    // Success: Reduce the stock
                    dbItem.setStockQuantity(dbItem.getStockQuantity() - reqItem.quantity);
                    dbSession.merge(dbItem);
                }

                // 4. If all items have sufficient stock, commit the transaction
                tx.commit();
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\": \"Inventory successfully decremented.\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"message\": \"Internal Server Error processing inventory update.\"}");
            e.printStackTrace();
        }
    }
}