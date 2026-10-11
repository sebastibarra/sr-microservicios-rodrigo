package com.reservas.ms_notificaciones.mail;

import com.reservas.commons.exception.CommonsException;

public class MailDeliveryException extends CommonsException {

    public MailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
