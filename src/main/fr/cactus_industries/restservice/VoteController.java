package fr.cactus_industries.restservice;

import java.util.List;

import fr.cactus_industries.query.Sondage;
import fr.cactus_industries.query.Proposition;
import fr.cactus_industries.restservice.survey.Survey;
import fr.cactus_industries.restservice.survey.PropositionRDV;
import fr.cactus_industries.restservice.login.LogIn;
import fr.cactus_industries.restservice.login.LoggedTokenInfo;
import fr.cactus_industries.restservice.survey.Voting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoteController {

    @GetMapping("/vote/add")
    public Response add(@RequestParam(value="associatedProposition", defaultValue = "") int associatedProposition,
                        @RequestParam(value="token", defaultValue = "") String token) {
        LoggedTokenInfo tokenInfo = LogIn.login(token);
        Proposition proposition = PropositionRDV.getPropositionById(associatedProposition);
        if(proposition==null) {
            return new FailResponse(FailResponse.Reason.INVALIDIDOFPROPOSITION);
        }
        if (tokenInfo == null) {
            return new FailResponse(FailResponse.Reason.INVALIDTOKEN);
        } else {
            int associatedUser = tokenInfo.getID();
            if (associatedUser==0) {
                return new FailResponse(FailResponse.Reason.NOAUTHOR);
            }
            else if(!Survey.checkAlreadyVote(associatedProposition, associatedUser)){
                int result = Voting.addVote(associatedProposition, associatedUser);
                if(result==-1) {
                    return new FailResponse(FailResponse.Reason.GENERIC);
                }
                else {
                    return new Response("OK");
                }
            }
            else {
                return new FailResponse(FailResponse.Reason.YOUHAVEALREADYVOTED);
            }
        }
    }

    @GetMapping("/vote/listOfPropositionsForSondage")
    public List<Proposition> listOfPropositionsForSondage(@RequestParam(value="associatedSurvey", defaultValue = "") int associatedSurvey) {
        List<Proposition> list = PropositionRDV.getListOfPropositionsBySondageId(associatedSurvey);
        return list;
    }

    @GetMapping("/vote/remove")
    public Response remove(@RequestParam(value="id", defaultValue = "") int id,
                           @RequestParam(value="token", defaultValue = "") String token) {
        LoggedTokenInfo tokenInfo = LogIn.login(token);
        if (tokenInfo == null) {
            return new FailResponse(FailResponse.Reason.INVALIDTOKEN);
        } else {
            int authorId = tokenInfo.getID();
            if (authorId==0) {
                return new FailResponse(FailResponse.Reason.NOAUTHOR);
            }
            else {
                if((PropositionRDV.removeProposition(id)==0)) {
                    return new FailResponse(FailResponse.Reason.GENERIC);
                }
                else {
                    return new Response("OK");
                }
            }
        }
    }

}