package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;
import com.syttech.syttech.scheduler.scheduler.domain.command.RefreshTokenCommand;

/**
 * Porta de entrada (caso de uso) responsável por renovar a sessão do cliente.
 *
 * <p>Recebe um refresh token previamente emitido e, se ele for válido e não estiver expirado,
 * devolve um novo par access/refresh sem exigir credenciais novamente.
 *
 * <p>Falhas (token inválido, expirado ou cliente removido) devem ser sinalizadas com {@code
 * UnauthenticatedException}, mapeada para HTTP {@code 401} pelo adaptador web.
 */
public interface RefreshTokenUseCase {

    /**
     * Troca um refresh token válido por um novo par de tokens.
     *
     * @param command comando contendo o refresh token a ser validado
     * @return novo {@link LoginResult} com access/refresh atualizados e dados do cliente
     */
    LoginResult refresh(RefreshTokenCommand command);
}
