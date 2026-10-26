package com.storemanagement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        // 1. Get credentials from the request (Using Form Data for simplicity)
        String userParam = req.getParameter("username");
        String passParam = req.getParameter("password");

        // 2. Open a database session
        try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {
            
            // 3. Search for the user in PostgreSQL
            Query<User> query = dbSession.createQuery("FROM User WHERE username = :username AND password = :password", User.class);
            query.setParameter("username", userParam);
            query.setParameter("password", passParam);
            
            User foundUser = query.uniqueResult();

            if (foundUser != null) {
                // This creates a secure session on the Tomcat server
                HttpSession httpSession = req.getSession(true); 
                httpSession.setAttribute("userId", foundUser.getId());
                httpSession.setAttribute("role", foundUser.getRole());

                // Send success response back to the client
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\": \"Login successful\", \"role\": \"" + foundUser.getRole() + "\", \"userId\": \"" + foundUser.getId() + "\"}");
            } else {
                // Invalid credentials
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\": \"Invalid username or password\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Database connection error\"}");
            e.printStackTrace();
        }
    }
}