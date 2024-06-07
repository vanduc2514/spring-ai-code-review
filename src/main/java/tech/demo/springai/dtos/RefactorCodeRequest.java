package tech.demo.springai.dtos;

import java.util.List;

import lombok.Data;

@Data
public class RefactorCodeRequest {

    private String codeSnippet;

    private List<AgentResponse> assessments;
}
