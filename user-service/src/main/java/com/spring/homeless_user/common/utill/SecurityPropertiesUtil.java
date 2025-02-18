package com.spring.homeless_user.common.utill;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityPropertiesUtil {

    private List<String> excludedPaths;

}
