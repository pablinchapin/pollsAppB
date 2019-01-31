/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.repository;

import com.pablinchapin.polls.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author pvargas
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    List<User> findByIdIn(List<Long> userIds);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    
    
}
