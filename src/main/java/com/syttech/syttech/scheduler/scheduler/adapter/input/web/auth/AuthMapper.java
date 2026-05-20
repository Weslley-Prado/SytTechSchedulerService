package com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.AuthenticatedCustomer;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.LoginRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.LoginResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.RefreshTokenRequest;
import com.syttech.syttech.scheduler.scheduler.domain.command.LoginCommand;
import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;
import com.syttech.syttech.scheduler.scheduler.domain.command.RefreshTokenCommand;

/**
 * Conversores entre os DTOs gerados pelo OpenAPI e os tipos de domínio da API de autenticação.
 *
 * <p>Mantém o adaptador web desacoplado das classes geradas: nenhum caso de uso conhece {@code
 * LoginRequest}/{@code RefreshTokenRequest}/{@code LoginResponse}.
 *
 * <p>Classe utilitária — não deve ser instanciada.
 */
final class AuthMapper {

    private AuthMapper() {}

    /**
     * Converte o DTO de login no comando de domínio correspondente.
     *
     * @param req payload validado vindo do controller
     * @return comando consumido por {@code LoginUseCase}
     */
    static LoginCommand toCommand(final LoginRequest req) {
        return new LoginCommand(req.getEmail(), req.getPassword());
    }

    /**
     * Converte o DTO de refresh no comando de domínio correspondente.
     *
     * @param req payload contendo o refresh token
     * @return comando consumido por {@code RefreshTokenUseCase}
     */
    static RefreshTokenCommand toCommand(final RefreshTokenRequest req) {
        return new RefreshTokenCommand(req.getRefreshToken());
    }

    /**
     * Converte o resultado de domínio na resposta HTTP esperada pelo contrato.
     *
     * @param r resultado produzido pelo caso de uso (login ou refresh)
     * @return DTO de resposta serializado para o cliente HTTP
     */
    static LoginResponse toResponse(final LoginResult r) {
        return new LoginResponse()
                .accessToken(r.accessToken())
                .refreshToken(r.refreshToken())
                .expiresIn((int) r.expiresIn())
                .tokenType("Bearer")
                .customer(
                        new AuthenticatedCustomer()
                                .id(r.customerId())
                                .fullName(r.fullName())
                                .email(r.email())
                                .emailVerified(r.emailVerified()));
    }
}
