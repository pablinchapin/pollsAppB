/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.security;

import com.pablinchapin.polls.model.User;
import com.pablinchapin.polls.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author pvargas
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String string) throws UsernameNotFoundException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        User user = userRepository.findByUsernameOrEmail(string, string)
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + string));
        
        return UserPrincipal.create(user);
    }
    
    
    @Transactional
    public UserDetails loadUserById(Long id){
        
        User user = userRepository.findById(id)
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return UserPrincipal.create(user);
        
    }
    
}
