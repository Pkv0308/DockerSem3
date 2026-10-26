package com.storemanagement;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// The "/*" means apply this bouncer to EVERY endpoint
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
            
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. Dynamically grab the frontend's URL and tell the browser it is allowed
        String origin = req.getHeader("Origin");
        if (origin != null) {
            res.setHeader("Access-Control-Allow-Origin", origin);
        }

        // 2. Allow specific methods and headers
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        // 3. CRITICAL: Allow Tomcat to send and receive the JSESSIONID cookie across different ports
        res.setHeader("Access-Control-Allow-Credentials", "true");

        // 4. Handle "Preflight" requests (Browsers send an OPTIONS request first to check permissions)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Pass the request down the chain to your actual Servlets
        chain.doFilter(request, response);
    }
}