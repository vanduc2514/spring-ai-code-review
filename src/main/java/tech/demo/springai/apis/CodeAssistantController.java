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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import tech.demo.springai.dtos.AgentResponse;
import tech.demo.springai.dtos.AgentType;
import tech.demo.springai.dtos.RefactorCodeRequest;
import tech.demo.springai.dtos.RefactorCodeResponse;
import tech.demo.springai.service.LogicAssementAgent;
import tech.demo.springai.service.QualityAssessmentAgent;
import tech.demo.springai.service.RefactorAgent;
import tech.demo.springai.service.PerformanceAssessmentAgent;
import tech.demo.springai.service.SecurityAssessmentAgent;


@RestController
@RequestMapping("/assistant/code")
public class CodeAssistantController {

    @Autowired
    private LogicAssementAgent logicAssementAgent;

    @Autowired
    private QualityAssessmentAgent qualityAssessmentAgent;

    @Autowired
    private PerformanceAssessmentAgent performanceAssessmentAgent;

    @Autowired
    private SecurityAssessmentAgent securityAssessmentAgent;

    @Autowired
    private RefactorAgent refactorAgent;

    @PostMapping("/review/logic")
    public Flux<AgentResponse> streamCodeReviewLogic(@RequestBody String codeSnippet) {
        return logicAssementAgent.assessCodeSnippet(codeSnippet)
                        .map(chatResponse -> mapAgentResponse(
                                chatResponse, AgentType.LOGIC_ASSESSMENT));
    }

    @PostMapping("/review/quality")
    public Flux<AgentResponse> streamCodeReviewQuality(@RequestBody String codeSnippet) {
        return qualityAssessmentAgent.assessCodeSnippet(codeSnippet)
                        .map(chatResponse -> mapAgentResponse(
                                chatResponse, AgentType.QUALITY_ASSESSMENT));
    }

    @PostMapping("/review/performance")
    public Flux<AgentResponse> streamCodeReviewPerformance(@RequestBody String codeSnippet) {
        return performanceAssessmentAgent.assessCodeSnippet(codeSnippet)
                        .map(chatResponse -> mapAgentResponse(
                                chatResponse, AgentType.PERFORMANCE_ASSESSMENT));
    }


    @PostMapping("/review/security")
    public Flux<AgentResponse> streamCodeReviewSecurity(@RequestBody String codeSnippet) {
        return securityAssessmentAgent.assessCodeSnippet(codeSnippet)
                        .map(chatResponse -> mapAgentResponse(
                                chatResponse, AgentType.SECURITY_ASSESSMENT));
    }

    @PostMapping(
        value = "/refactor",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_NDJSON_VALUE
    )
    public Flux<RefactorCodeResponse> streamRefactor(@RequestBody RefactorCodeRequest refactorCodeRequest) {
        var assessmentMap = toAssessmentMap(refactorCodeRequest.getAssessments());
        return refactorAgent.refactorCode(
                refactorCodeRequest.getCodeSnippet(),
                assessmentMap.getOrDefault(AgentType.LOGIC_ASSESSMENT, ""),
                assessmentMap.getOrDefault(AgentType.QUALITY_ASSESSMENT, ""),
                assessmentMap.getOrDefault(AgentType.SECURITY_ASSESSMENT, ""),
                assessmentMap.getOrDefault(AgentType.PERFORMANCE_ASSESSMENT, ""))
            .map(this::mapRefactorCodeResponse);
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

    private RefactorCodeResponse mapRefactorCodeResponse(ChatResponse chatResponse) {
        return RefactorCodeResponse.builder()
                .refactoredCode(getChatResponseContent(chatResponse))
                .build();
    }

    private String getChatResponseContent(ChatResponse chatResponse) {
        return Optional.ofNullable(chatResponse.getResult())
                .map(Generation::getOutput)
                .map(AssistantMessage::getContent)
                .orElse("");
    }

}
