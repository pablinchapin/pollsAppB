/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author pvargas
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest hsreq, 
            HttpServletResponse hsres, 
            FilterChain fc) throws ServletException, IOException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        try {
        
            String jwt = getJwtFromRequest(hsreq);
            
            if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)){
            
                Long userId = tokenProvider.getUserIdFromJWT(jwt);
                
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(hsreq));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            
            }
        
        } catch (Exception ex){
            logger.error("Could not set user authentication in security context ", ex);
        }
        
        fc.doFilter(hsreq, hsres);
        
    }
    
    
    
    private String getJwtFromRequest(HttpServletRequest hsreq){
    
        String bearerToken =  hsreq.getHeader("Authorization");
        
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7, bearerToken.length());
        }
        
        return null;
    }
    
}
