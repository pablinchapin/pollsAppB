/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 *
 * @author pvargas
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint{
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    

    @Override
    public void commence(
            HttpServletRequest hsreq, 
            HttpServletResponse hsres, 
            AuthenticationException ae) throws IOException, ServletException {
        
        logger.error("Responding with unauthorized error. Message -{} ", ae.getMessage());
        
        hsres.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
    }
    
}
