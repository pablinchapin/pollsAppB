/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.controller;

import com.pablinchapin.polls.exception.ResourceNotFoundException;
import com.pablinchapin.polls.model.User;
import com.pablinchapin.polls.payload.PagedResponse;
import com.pablinchapin.polls.payload.PollResponse;
import com.pablinchapin.polls.payload.UserIdentityAvailability;
import com.pablinchapin.polls.payload.UserProfile;
import com.pablinchapin.polls.payload.UserSummary;
import com.pablinchapin.polls.repository.PollRepository;
import com.pablinchapin.polls.repository.UserRepository;
import com.pablinchapin.polls.repository.VoteRepository;
import com.pablinchapin.polls.security.CurrentUser;
import com.pablinchapin.polls.security.UserPrincipal;
import com.pablinchapin.polls.service.PollService;
import com.pablinchapin.polls.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author pvargas
 */

@RestController
@RequestMapping("/api")
public class UserController {
    
    private UserRepository userRepository;
    private PollRepository pollRepository;
    private VoteRepository voteRepository;
    private PollService pollService;
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    
    @Autowired
    public UserController(UserRepository userRepository, PollRepository pollRepository, VoteRepository voteRepository, PollService pollService) {
        this.userRepository = userRepository;
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
        this.pollService = pollService;
    }
    
    
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUSer(@CurrentUser UserPrincipal currentUser){
        
        UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
    
        return userSummary;
    }
    
    
    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value="username") String username){
    
        Boolean isAvailable = !userRepository.existsByUsername(username);
        
        return new UserIdentityAvailability(isAvailable);
    }
    
    
    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value="email") String email){
    
        Boolean isAvailable = !userRepository.existsByEmail(email);
        
        return new UserIdentityAvailability(isAvailable);
    
    }
    
    
    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username){
        
        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        Long pollCount = pollRepository.countByCreatedBy(user.getId());
        Long voteCount = voteRepository.countByUserId(user.getId());
        
        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), pollCount, voteCount);
    
        return userProfile;
    }
    
    
    @GetMapping("/users/{username}/polls")
    public PagedResponse<PollResponse> getPollsCreatedBy(
            @PathVariable(value = "username") String username,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ){
    
        return pollService.getPollsCreatedBy(username, currentUser, page, size);
    }
    
    
    
    @GetMapping("/users/{username}/votes")
    public PagedResponse<PollResponse> getPollsVotedBy(
            @PathVariable(value = "username") String username,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ){
    
        return pollService.getPollsVotedBy(username, currentUser, page, size);
    }
    
    
    
}
