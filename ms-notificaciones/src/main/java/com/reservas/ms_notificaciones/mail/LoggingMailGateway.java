package com.reservas.ms_notificaciones.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailGateway.class);

    @Override
    public void send(String to, String subject, String htmlBody) {
        log.info("Mail delivery disabled, email to {} with subject '{}' only logged", to, subject);
        log.debug("Email body for {}: {}", to, htmlBody);
    }
}
