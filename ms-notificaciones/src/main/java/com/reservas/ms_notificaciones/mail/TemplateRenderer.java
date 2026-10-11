package com.reservas.ms_notificaciones.mail;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.]+)\\s*}}");

    public String renderHtml(String template, Map<String, Object> values) {
        return render(template, values, true);
    }

    public String renderSubject(String template, Map<String, Object> values) {
        return render(template, values, false).replaceAll("[\\r\\n]+", " ").trim();
    }

    private String render(String template, Map<String, Object> values, boolean escape) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            Object value = values == null ? null : values.get(matcher.group(1));
            String text = value == null ? "" : String.valueOf(value);
            matcher.appendReplacement(result, Matcher.quoteReplacement(escape ? HtmlUtils.htmlEscape(text) : text));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
