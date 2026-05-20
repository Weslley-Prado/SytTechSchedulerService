package com.syttech.syttech.scheduler.scheduler.adapter.output.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para o envio de e-mails transacionais.
 *
 * <p>Mapeia o prefixo {@code syttech.email.*} das {@code application.properties}. Funciona em
 * conjunto com as propriedades nativas {@code spring.mail.*} do Spring Boot Starter Mail, que
 * configuram o {@code JavaMailSender}.
 */
@ConfigurationProperties(prefix = "syttech.email")
public class EmailProperties {

    /**
     * Quando {@code false} (padrão), o {@link LoggingEmailNotificationAdapter} é ativado e nenhum
     * e-mail real é enviado. Quando {@code true}, o {@link SmtpEmailNotificationAdapter} assume.
     */
    private boolean enabled;

    /** Endereço usado no campo {@code From} das mensagens enviadas. */
    private String from = "no-reply@syttech.local";

    /**
     * URL base pública da aplicação, usada para montar links absolutos nos e-mails (por exemplo, o
     * link de verificação de e-mail e o link público do agendamento).
     */
    private String appBaseUrl = "http://localhost:8082";

    /**
     * @return {@code true} se o envio real de e-mails está habilitado.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled habilita ou desabilita o envio real de e-mails.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return endereço remetente configurado.
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from novo endereço remetente.
     */
    public void setFrom(final String from) {
        this.from = from;
    }

    /**
     * @return URL base pública usada na montagem de links dos e-mails.
     */
    public String getAppBaseUrl() {
        return appBaseUrl;
    }

    /**
     * @param appBaseUrl nova URL base pública.
     */
    public void setAppBaseUrl(final String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
    }
}
