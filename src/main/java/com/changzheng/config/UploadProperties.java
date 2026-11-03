package com.changzheng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.nio.file.Path;

@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {
    private String directory = "data/uploads";
    private String publicBaseUrl = "";

    public Path avatarDirectory() {
        return Path.of(directory).toAbsolutePath().normalize().resolve("avatars");
    }
}
