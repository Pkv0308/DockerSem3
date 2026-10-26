package com.storemanagement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// This annotation tells Tomcat to route requests for "/test" to this class
@WebServlet("/test") 
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Set the response type to plain text
        resp.setContentType("text/plain");
        
        // Write a message back to the browser or Postman
        resp.getWriter().write("Tomcat is alive, the Servlet is working, and the web layer is officially online!");
    }
}