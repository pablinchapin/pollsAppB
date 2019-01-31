/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pablinchapin.polls.util;

import com.pablinchapin.polls.model.Poll;
import com.pablinchapin.polls.model.User;
import com.pablinchapin.polls.payload.ChoiceResponse;
import com.pablinchapin.polls.payload.PollResponse;
import com.pablinchapin.polls.payload.UserSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author pvargas
 */
public class ModelMapper {
    
    
    public static PollResponse mapPollToPollResponse(Poll poll, Map<Long, Long> choiceVotesMap, User creator, Long userVote){
        
        Instant now = Instant.now();
        PollResponse pollResponse = new PollResponse();
        
        pollResponse.setId(poll.getId());
        pollResponse.setQuestion(poll.getQuestion());
        pollResponse.setCreationDateTime(poll.getCreatedAt());
        pollResponse.setExpirationDateTime(poll.getExpirationDateTime());
        pollResponse.setIsExpired(poll.getExpirationDateTime().isBefore(now));
        
        List<ChoiceResponse> choiceResponses = poll.getChoices().stream().map(choice -> {
            ChoiceResponse choiceResponse = new ChoiceResponse();
            
            choiceResponse.setId(choice.getId());
            choiceResponse.setText(choice.getText());
            
            if(choiceVotesMap.containsKey(choice.getId())){
                choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
            }else{
                    choiceResponse.setVoteCount(0L);
            }
        
            return choiceResponse;
        }).collect(Collectors.toList());
        
        
        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName());
        
        pollResponse.setChoices(choiceResponses);
        pollResponse.setCreatedBy(creatorSummary);
        
        if(userVote != null){
            pollResponse.setSelectedChoice(userVote);
        }
        
        Long totalVotes = pollResponse.getChoices().stream().mapToLong(ChoiceResponse::getVoteCount).sum();
        
        pollResponse.setTotalVotes(totalVotes);
        
        
        return pollResponse;
    
    
    }
    
    
    
}
