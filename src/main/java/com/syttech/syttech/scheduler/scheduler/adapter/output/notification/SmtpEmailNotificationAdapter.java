package com.syttech.syttech.scheduler.scheduler.adapter.output.notification;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.EmailNotificationPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Adaptador real do {@link EmailNotificationPort} que envia e-mails via SMTP usando o {@link
 * JavaMailSender} (Jakarta Mail) provido pelo Spring Boot Starter Mail.
 *
 * <p>É ativado pela propriedade {@code syttech.email.enabled=true}. Quando desativado, o {@link
 * LoggingEmailNotificationAdapter} é usado em seu lugar.
 *
 * <p>Configuração externa esperada:
 *
 * <ul>
 *   <li>{@code syttech.email.from} — endereço remetente
 *   <li>{@code syttech.email.app-base-url} — URL base usada nos links
 *   <li>{@code spring.mail.host}, {@code spring.mail.port}, {@code spring.mail.username}, {@code
 *       spring.mail.password}, {@code spring.mail.properties.mail.smtp.*}
 * </ul>
 *
 * <p>Falhas no envio (servidor inacessível, credenciais inválidas) são apenas registradas em log,
 * sem propagar exceção, para não impactar o fluxo principal da requisição que originou a
 * notificação.
 */
@Component
@EnableConfigurationProperties(EmailProperties.class)
@ConditionalOnProperty(prefix = "syttech.email", name = "enabled", havingValue = "true")
public class SmtpEmailNotificationAdapter implements EmailNotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(SmtpEmailNotificationAdapter.class);

    /** Cliente SMTP autoconfigurado pelo Spring a partir de {@code spring.mail.*}. */
    private final JavaMailSender mailSender;

    /** Repositório de agendamentos — usado para hidratar os dados do e-mail. */
    private final AppointmentRepositoryPort appointments;

    /** Repositório de clientes — usado para obter o destinatário e o nome no corpo do e-mail. */
    private final CustomerRepositoryPort customers;

    /** Propriedades específicas da aplicação (remetente, URL base, flag de ativação). */
    private final EmailProperties props;

    /**
     * Construtor com injeção de dependências.
     *
     * @param mailSender cliente SMTP autoconfigurado pelo Spring Boot
     * @param appointments repositório para localizar o agendamento a ser notificado
     * @param customers repositório para localizar o cliente destinatário
     * @param props propriedades do bloco {@code syttech.email}
     */
    public SmtpEmailNotificationAdapter(
            final JavaMailSender mailSender,
            final AppointmentRepositoryPort appointments,
            final CustomerRepositoryPort customers,
            final EmailProperties props) {
        this.mailSender = mailSender;
        this.appointments = appointments;
        this.customers = customers;
        this.props = props;
    }

    /**
     * Envia o e-mail de confirmação de agendamento contendo o código público.
     *
     * <p>Se o agendamento ou o cliente não forem encontrados, apenas registra um {@code warn} e
     * retorna sem lançar exceção.
     *
     * @param appointmentId identificador do agendamento confirmado
     */
    @Override
    public void sendAppointmentCode(final UUID appointmentId) {
        Appointment appointment = appointments.findById(appointmentId).orElse(null);
        if (appointment == null) {
            LOG.warn("[EMAIL] appointment not found: {}", appointmentId);
            return;
        }
        Customer customer = customers.findById(appointment.customerId()).orElse(null);
        if (customer == null || customer.email() == null) {
            LOG.warn("[EMAIL] no recipient for appointment={}", appointmentId);
            return;
        }
        var msg = new SimpleMailMessage();
        msg.setFrom(props.getFrom());
        msg.setTo(customer.email());
        msg.setSubject("[SytTech] Confirmacao de agendamento — codigo " + appointment.code());
        msg.setText(buildAppointmentBody(appointment, customer));
        send(msg, "appointment-code", appointmentId);
    }

    /**
     * Envia o e-mail de verificação de cadastro com o link de confirmação.
     *
     * @param customerId identificador do cliente recém-cadastrado
     * @param token token opaco que valida o e-mail
     */
    @Override
    public void sendCustomerVerification(final UUID customerId, final String token) {
        Customer customer = customers.findById(customerId).orElse(null);
        if (customer == null || customer.email() == null) {
            LOG.warn("[EMAIL] no recipient for customer={}", customerId);
            return;
        }
        var msg = new SimpleMailMessage();
        msg.setFrom(props.getFrom());
        msg.setTo(customer.email());
        msg.setSubject("[SytTech] Confirme seu e-mail");
        msg.setText(buildVerificationBody(customer, token));
        send(msg, "customer-verification", customerId);
    }

    /** Monta o corpo textual do e-mail de confirmação de agendamento. */
    private String buildAppointmentBody(final Appointment a, final Customer c) {
        return "Ola "
                + c.fullName()
                + ",\n\n"
                + "Seu agendamento foi confirmado!\n"
                + "Codigo: "
                + a.code()
                + "\n"
                + "Inicio: "
                + a.start()
                + "\n\n"
                + "Para acompanhar, acesse "
                + props.getAppBaseUrl()
                + "/appointments/"
                + a.code()
                + "\n";
    }

    /** Monta o corpo textual do e-mail de verificação de cadastro. */
    private String buildVerificationBody(final Customer c, final String token) {
        return "Ola "
                + c.fullName()
                + ",\n\n"
                + "Confirme seu e-mail clicando no link abaixo:\n"
                + props.getAppBaseUrl()
                + "/auth/verify?token="
                + token
                + "\n\n"
                + "Se voce nao criou esta conta, ignore esta mensagem.\n";
    }

    /**
     * Despacha a mensagem para o servidor SMTP, encapsulando exceções em log para não interromper o
     * fluxo de negócio que originou o disparo.
     */
    private void send(final SimpleMailMessage msg, final String kind, final UUID id) {
        try {
            mailSender.send(msg);
            LOG.info("[EMAIL/{}] sent to={} id={}", kind, msg.getTo(), id);
        } catch (Exception e) {
            LOG.error("[EMAIL/{}] FAILED id={}: {}", kind, id, e.getMessage());
        }
    }
}
