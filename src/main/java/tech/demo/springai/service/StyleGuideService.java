package tech.demo.springai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StyleGuideService {

    @Value("classpath:java-style-guide.txt")
    private String styleGuide;

    // TODO: replace with actual call to external API
    public String getStyleGuideFor(String language) {
        System.out.println(language);
        return styleGuide;
    }

}
