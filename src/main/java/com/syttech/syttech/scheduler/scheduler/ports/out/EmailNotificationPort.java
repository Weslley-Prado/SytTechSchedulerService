package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.UUID;

/**
 * Porta de saída utilizada pelos listeners de domínio para disparar e-mails transacionais.
 *
 * <p>Permite trocar a implementação real (SMTP via Jakarta Mail, Mailgun, SES, etc.) sem afetar a
 * camada de aplicação. Por padrão, em ambientes onde {@code syttech.email.enabled=false}, é usada
 * uma implementação que apenas registra em log.
 */
public interface EmailNotificationPort {

    /**
     * Envia ao cliente o e-mail de confirmação de agendamento contendo o código público.
     *
     * @param appointmentId identificador do agendamento confirmado
     */
    void sendAppointmentCode(UUID appointmentId);

    /**
     * Envia o link de verificação de e-mail para um cliente recém-cadastrado.
     *
     * @param customerId identificador do cliente
     * @param token token opaco que valida o e-mail quando consumido pelo endpoint de verificação
     */
    void sendCustomerVerification(UUID customerId, String token);
}
