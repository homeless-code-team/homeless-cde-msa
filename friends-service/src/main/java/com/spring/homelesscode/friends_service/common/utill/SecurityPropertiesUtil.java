package com.spring.homelesscode.friends_service.common.utill;

import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityPropertiesUtil {

    private List<String> excludedPaths;

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }


}
