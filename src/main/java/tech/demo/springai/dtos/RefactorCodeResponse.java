package tech.demo.springai.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefactorCodeResponse {

    String refactoredCode;

}
