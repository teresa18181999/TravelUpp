package com.travelupp.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class SpaRedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();

        boolean isApi = path.startsWith("/api/");
        boolean isStatic = path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.contains(".");

        if (!"GET".equalsIgnoreCase(method) || isApi || isStatic) {
            chain.doFilter(request, response);
            return;
        }

        req.getRequestDispatcher("/index.html").forward(req, resp);
    }
}
