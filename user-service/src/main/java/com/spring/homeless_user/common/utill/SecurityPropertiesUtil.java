package com.spring.homeless_user.common.utill;

import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
