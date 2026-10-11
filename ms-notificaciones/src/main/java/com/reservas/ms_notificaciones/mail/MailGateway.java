package com.reservas.ms_notificaciones.mail;

public interface MailGateway {

    void send(String to, String subject, String htmlBody);
}
