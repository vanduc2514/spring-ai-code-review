package tech.demo.springai.apis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import tech.demo.springai.dtos.AgentResponse;
import tech.demo.springai.dtos.AgentType;
import tech.demo.springai.dtos.RefactorCodeRequest;
import tech.demo.springai.dtos.RefactorCodeResponse;
import tech.demo.springai.service.LogicAssementAgent;
import tech.demo.springai.service.QualityAssessmentAgent;
import tech.demo.springai.service.RefactorAgent;


@RestController
public class CodeAssistantController {

    @Autowired
    private LogicAssementAgent logicAssementAgent;

    @Autowired
    private QualityAssessmentAgent qualityAssessmentAgent;

    @Autowired
    private RefactorAgent refactorAgent;

    @PostMapping(
        value = "/stream/code/review/stage/0",
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.APPLICATION_NDJSON_VALUE
    )
    public Flux<AgentResponse> streamAssessment(@RequestBody String codeSnippet) {
        return Flux.merge(
                logicAssementAgent.assessCodeSnippet(codeSnippet)
                        .map(chatResponse -> mapAgentResponse(
                                chatResponse, AgentType.LOGIC_ASSESSMENT)),
                qualityAssessmentAgent.assessCodeSnippet(codeSnippet)
                        .map(chatResponse -> mapAgentResponse(
                                chatResponse, AgentType.QUALITY_ASSESSMENT)));
    }

    private Map<AgentType, String> toAssessmentMap(List<AgentResponse> assessments) {
        var assessmentMap = new HashMap<AgentType, String>(assessments.size());
        assessments.forEach(agent -> {
            assessmentMap.put(agent.getAgentType(), agent.getResponse());
        });
        return assessmentMap;
    }

    private AgentResponse mapAgentResponse(ChatResponse chatResponse, AgentType agentType) {
        return AgentResponse.builder()
                .agentType(agentType)
                .response(getChatResponseContent(chatResponse))
                .build();
    }

    private String getChatResponseContent(ChatResponse chatResponse) {
        return Optional.ofNullable(chatResponse.getResult())
                .map(Generation::getOutput)
                .map(AssistantMessage::getContent)
                .orElse("");
    }

}
