/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.service;

import com.pablinchapin.polls.exception.BadRequestException;
import com.pablinchapin.polls.exception.ResourceNotFoundException;
import com.pablinchapin.polls.model.Choice;
import com.pablinchapin.polls.model.ChoiceVoteCount;
import com.pablinchapin.polls.model.Poll;
import com.pablinchapin.polls.model.User;
import com.pablinchapin.polls.model.Vote;
import com.pablinchapin.polls.payload.PagedResponse;
import com.pablinchapin.polls.payload.PollRequest;
import com.pablinchapin.polls.payload.PollResponse;
import com.pablinchapin.polls.payload.VoteRequest;
import com.pablinchapin.polls.repository.PollRepository;
import com.pablinchapin.polls.repository.UserRepository;
import com.pablinchapin.polls.repository.VoteRepository;
import com.pablinchapin.polls.security.UserPrincipal;
import com.pablinchapin.polls.util.AppConstants;
import com.pablinchapin.polls.util.ModelMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 *
 * @author pvargas
 */

@Service
public class PollService {
    
    private PollRepository pollRepository;
    private VoteRepository voteRepository;
    private UserRepository userRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(PollService.class);
    
    
    @Autowired
    public PollService(PollRepository pollRepository, VoteRepository voteRepository, UserRepository userRepository) {
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
    }
    
    
    public PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size){
        
        validatePageNumberAndSize(page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findAll(pageable);
        
        if(polls.getNumberOfElements() == 0){
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }
        
        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());
        
        List<PollResponse> pollResponses = polls.map(poll -> {
            return ModelMapper.mapPollToPollResponse(
                    poll, 
                    choiceVoteCountMap, 
                    creatorMap.get(poll.getCreatedBy()), 
                     pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
        }).getContent();
        
        
        return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }
    
    
    public PagedResponse<PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page, int size){
    
        validatePageNumberAndSize(page, size);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);
        
        if(polls.getNumberOfElements() == 0){
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }
        
        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        
        List<PollResponse> pollResponses = polls.map(poll -> {
            return ModelMapper.mapPollToPollResponse(
                    poll, 
                    choiceVoteCountMap, 
                    user, 
                    pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
            }).getContent();
        
    
        return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }
    
    
    public PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size){
    
        validatePageNumberAndSize(page, size);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);
        
        if(userVotedPollIds.getNumberOfElements() == 0){
            return new PagedResponse<>(Collections.emptyList(), userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
        }
        
        List<Long> pollIds = userVotedPollIds.getContent();
        
        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");
        
        List<Poll> polls = pollRepository.findByIdIn(pollIds, sort);
        
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(polls);
        
        List<PollResponse> pollResponses = polls.stream().map(poll -> {
            return ModelMapper.mapPollToPollResponse(
                    poll, 
                    choiceVoteCountMap, 
                    creatorMap.get(poll.getCreatedBy()),
                    pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
            }).collect(Collectors.toList());
        
    
        return new PagedResponse<>(pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
    }
    
    
    public Poll createPoll(PollRequest pollRequest){
        
        Poll poll = new Poll();
    
        poll.setQuestion(pollRequest.getQuestion());
        
        pollRequest.getChoices().forEach(choiceRequest -> {
            poll.addChoice(new Choice(choiceRequest.getText()));
        });
    
        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
                                        .plus(Duration.ofHours(pollRequest.getPollLength().getHours()));
        
        poll.setExpirationDateTime(expirationDateTime);
            
        return pollRepository.save(poll);
    
    }
    
    
    public PollResponse getPollById(Long pollId, UserPrincipal currentUser){
    
        Poll poll = pollRepository.findById(pollId)
                                .orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));
        
        List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);
        
        Map<Long, Long> choiceVotesMap = votes.stream()
                                            .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
        
        User creator = userRepository.findById(poll.getCreatedBy())
                                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
        
        Vote userVote = null;
        if(currentUser != null){
            userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
        }
        
        
        return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, userVote != null ? userVote.getChoice().getId() : null);
    
    }
    
    
    public PollResponse castVoteAndGetUpdatedPoll(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser){
    
        Poll poll = pollRepository.findById(pollId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));
        
        if(poll.getExpirationDateTime().isBefore(Instant.now())){
            throw new BadRequestException("Sorry! this poll has already expired");
        }
        
        User user = userRepository.getOne(currentUser.getId());
        
        Choice selectedChoice = poll.getChoices().stream()
                                    .filter(choice -> choice.getId().equals(voteRequest.getChoiceId()))
                                    .findFirst()
                                    .orElseThrow(() -> new ResourceNotFoundException("Choice","id", voteRequest.getChoiceId()));
        
        Vote vote = new Vote();
        vote.setPoll(poll);
        vote.setUser(user);
        vote.setChoice(selectedChoice);
        
        try{
            vote = voteRepository.save(vote);
        }catch(DataIntegrityViolationException ex){
            
            logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
            
            throw new BadRequestException("Sorry! you have already cast your vote in this poll");
        }
        
        List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);
        
        
        Map<Long, Long> choiceVotesMap = votes.stream()
                                        .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
        
        
        User creator = userRepository.findById(poll.getCreatedBy())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

          
        return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, vote.getChoice().getId());
        
        
    }
    
    //IM
    private void validatePageNumberAndSize(int page, int size){
        if(page < 0){
            throw new BadRequestException("Page number cannot be less than zero.");
        }
        
        if(size > AppConstants.MAX_PAGE_SIZE){
            throw new BadRequestException("Page size must not be greater than "+AppConstants.MAX_PAGE_SIZE);
        }
    }
    
    private Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds){
    
        List<ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);
        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
        
        return choiceVotesMap;
    }
    
    private Map<Long, Long> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds){
    
        Map<Long, Long> pollUserVoteMap = null;
        
        if(currentUser != null){
            List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);
            
            pollUserVoteMap = userVotes.stream()
                    .collect(Collectors.toMap(vote -> vote.getPoll().getId(),vote -> vote.getChoice().getId() ));
        }
        
        
        return pollUserVoteMap;
    }
    
    
    Map<Long, User> getPollCreatorMap(List<Poll> polls){
        
        List<Long> creatorIds = polls.stream()
                .map(Poll::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());
        
        List<User> creators = userRepository.findByIdIn(creatorIds);
    
        Map<Long, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        
        return creatorMap;
    }
    
    
    
    
    
}
