package com.lixh.webexample.service.freemarker;

import com.lixh.webexample.ex.SystemException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Service
@Slf4j
public class FreeMarkerService {

    @Autowired
    private TemplateService templateService;

    public String processTemplate(String templatePath, Map<String, Object> model) {
        try {
            String templateContent = templateService.loadTemplate(templatePath);
            Template template = new Template("templateName", new StringReader(templateContent), null);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new SystemException(e);
        }

    }
}