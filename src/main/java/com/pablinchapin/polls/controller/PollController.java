/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.controller;

import com.pablinchapin.polls.model.Poll;
import com.pablinchapin.polls.payload.ApiResponse;
import com.pablinchapin.polls.payload.PagedResponse;
import com.pablinchapin.polls.payload.PollRequest;
import com.pablinchapin.polls.payload.PollResponse;
import com.pablinchapin.polls.payload.VoteRequest;
import com.pablinchapin.polls.repository.PollRepository;
import com.pablinchapin.polls.repository.UserRepository;
import com.pablinchapin.polls.repository.VoteRepository;
import com.pablinchapin.polls.security.CurrentUser;
import com.pablinchapin.polls.security.UserPrincipal;
import com.pablinchapin.polls.service.PollService;
import com.pablinchapin.polls.util.AppConstants;
import java.net.URI;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author pvargas
 */

@RestController
@RequestMapping("/api/polls")
public class PollController {
    
    private PollRepository pollRepository;
    private VoteRepository voteRepository;
    private UserRepository userRepository;
    private PollService pollService;
    
    private static final Logger logger = LoggerFactory.getLogger(PollController.class);

    @Autowired
    public PollController(PollRepository pollRepository, VoteRepository voteRepository, UserRepository userRepository, PollService pollService) {
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.pollService = pollService;
    }
    
    
    @GetMapping
    public PagedResponse<PollResponse> getPolls(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
    
        return pollService.getAllPolls(currentUser, page, size);
    }
    
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPoll(@Valid @RequestBody PollRequest pollRequest){
    
        Poll poll = pollService.createPoll(pollRequest);
        
        URI location = ServletUriComponentsBuilder
                                    .fromCurrentRequest().path("/{pollId}")
                .buildAndExpand(poll.getId()).toUri();
                
    
        return ResponseEntity.created(location)
                                .body(new ApiResponse(true, "Poll created successfully"));
    }
    
    
    @GetMapping("/{pollId}")
    public PollResponse getPollById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long pollId){
    
        return pollService.getPollById(pollId, currentUser);
    }
    
    
    @PostMapping("/{pollId}/votes")
    @PreAuthorize("hasRole('USER')")
    public PollResponse castVote(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long pollId,
            @Valid @RequestBody VoteRequest voteRequest
    ){
        return pollService.castVoteAndGetUpdatedPoll(pollId, voteRequest, currentUser);
    }
    
    

}
