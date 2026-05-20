package com.syttech.syttech.scheduler.scheduler.adapter.output.notification;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.ports.out.EmailNotificationPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Adaptador padrão de e-mail que apenas registra a mensagem em log — não envia nada de verdade.
 *
 * <p>É ativado quando a propriedade {@code syttech.email.enabled} está ausente ou definida como
 * {@code false}. É substituído pelo {@link SmtpEmailNotificationAdapter} quando {@code
 * syttech.email.enabled=true}.
 *
 * <p>Útil em desenvolvimento, testes e cenários em que não há servidor SMTP disponível, evitando a
 * necessidade de subir containers ou dependências externas.
 */
@Component
@EnableConfigurationProperties(EmailProperties.class)
@ConditionalOnProperty(
        prefix = "syttech.email",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true)
public class LoggingEmailNotificationAdapter implements EmailNotificationPort {

    private static final Logger LOG =
            LoggerFactory.getLogger(LoggingEmailNotificationAdapter.class);

    /**
     * Apenas loga a tentativa de envio de confirmação de agendamento.
     *
     * @param appointmentId identificador do agendamento confirmado
     */
    @Override
    public void sendAppointmentCode(final UUID appointmentId) {
        LOG.info("[EMAIL] would send appointment confirmation: appointmentId={}", appointmentId);
    }

    /**
     * Apenas loga a tentativa de envio do link de verificação de e-mail.
     *
     * @param customerId identificador do cliente
     * @param token token opaco de verificação
     */
    @Override
    public void sendCustomerVerification(final UUID customerId, final String token) {
        LOG.info(
                "[EMAIL] would send verification token to customer={} token={}", customerId, token);
    }
}
