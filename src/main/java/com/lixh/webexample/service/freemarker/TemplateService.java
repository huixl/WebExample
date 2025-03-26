package com.lixh.webexample.service.freemarker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Service
public class TemplateService {

    @Autowired
    private ResourceLoader resourceLoader;

    public String loadTemplate(String templatePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int numCharsRead;
            while ((numCharsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, numCharsRead);
            }
            return content.toString();
        }
    }
}