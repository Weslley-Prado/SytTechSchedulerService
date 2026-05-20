package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;
import com.syttech.syttech.scheduler.scheduler.domain.command.RefreshTokenCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.in.RefreshTokenUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.TokenIssuerPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.TokenVerifierPort;
import com.syttech.syttech.scheduler.shared.kernel.UnauthenticatedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do caso de uso {@link RefreshTokenUseCase}.
 *
 * <p>Fluxo:
 *
 * <ol>
 *   <li>Valida o refresh token através do {@link TokenVerifierPort} (assinatura, expiração, tipo).
 *   <li>Carrega o {@link Customer} associado pelo {@link CustomerRepositoryPort}.
 *   <li>Emite um novo par access/refresh através do {@link TokenIssuerPort}.
 * </ol>
 *
 * <p>Em qualquer falha (token inválido/expirado ou cliente inexistente) lança {@link
 * UnauthenticatedException}, que o {@code GlobalExceptionHandler} traduz para HTTP {@code 401
 * application/problem+json}.
 *
 * <p>Anotado como {@code @Transactional(readOnly = true)} pois nenhum estado é alterado — a emissão
 * de JWT é stateless.
 */
@Service
@Transactional(readOnly = true)
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    /** Porta de saída que valida assinatura/expiração do refresh token. */
    private final TokenVerifierPort verifier;

    /** Porta de saída para localizar o cliente referenciado pelo token. */
    private final CustomerRepositoryPort customers;

    /** Porta de saída que emite o novo par access/refresh. */
    private final TokenIssuerPort tokens;

    /**
     * Construtor com injeção das portas de saída.
     *
     * @param verifier verificador de tokens (assinatura/expiração/tipo)
     * @param customers repositório de clientes
     * @param tokens emissor de tokens JWT
     */
    public RefreshTokenUseCaseImpl(
            final TokenVerifierPort verifier,
            final CustomerRepositoryPort customers,
            final TokenIssuerPort tokens) {
        this.verifier = verifier;
        this.customers = customers;
        this.tokens = tokens;
    }

    /**
     * Executa o caso de uso de renovação.
     *
     * @param command comando com o refresh token recebido
     * @return novo {@code LoginResult} com tokens recém-emitidos
     * @throws UnauthenticatedException se o token for inválido/expirado ou o cliente não existir
     *     mais
     */
    @Override
    public LoginResult refresh(final RefreshTokenCommand command) {
        UUID customerId =
                verifier.verifyRefresh(command.refreshToken())
                        .orElseThrow(
                                () ->
                                        new UnauthenticatedException(
                                                "Invalid or expired refresh token"));
        Customer customer =
                customers
                        .findById(customerId)
                        .orElseThrow(
                                () -> new UnauthenticatedException("Customer no longer exists"));
        return tokens.issueFor(customer);
    }
}
