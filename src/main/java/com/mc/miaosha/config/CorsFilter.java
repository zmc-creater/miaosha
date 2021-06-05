package com.mc.miaosha.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@Component
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "http://localhost");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST");
       // httpServletResponse.setHeader("Access-Control-Allow-Headers","sessionToken");

        filterChain.doFilter(servletRequest,servletResponse);
    }
}
