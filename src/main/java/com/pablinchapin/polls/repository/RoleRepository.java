/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.repository;

import com.pablinchapin.polls.model.Role;
import com.pablinchapin.polls.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author pvargas
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    
    
    Optional<Role> findByName(RoleName roleName);
        
    
}
