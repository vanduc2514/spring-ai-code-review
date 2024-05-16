package tech.demo.springai.apis;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import tech.demo.springai.dtos.AgentResponse;
import tech.demo.springai.dtos.AgentType;
import tech.demo.springai.service.LogicAssementAgent;
import tech.demo.springai.service.QualityAssessmentAgent;


@RestController
public class CodeAssistantController {

    @Autowired
    private LogicAssementAgent logicAssementAgent;

    @Autowired
    private QualityAssessmentAgent qualityAssessmentAgent;

    @PostMapping("/stream/code/review/stage/0")
    public Flux<AgentResponse> streamCodeReviewStage0(@RequestBody String codeSnippet) {
        return Flux.merge(
                logicAssementAgent.assessCodeSnippet(codeSnippet)
                    .map(chatResponse -> mapAgentResponse(
                        chatResponse, AgentType.LOGIC_ASSESSMENT)),
                qualityAssessmentAgent.assessCodeSnippet(codeSnippet)
                    .map(chatResponse -> mapAgentResponse(
                        chatResponse, AgentType.QUALITY_ASSESSMENT)));
    }

    private AgentResponse mapAgentResponse(ChatResponse chatResponse, AgentType agentType) {
        return AgentResponse.builder()
                .agentType(agentType)
                .response(chatResponse.getResult().getOutput().getContent())
                .build();
    }
}
