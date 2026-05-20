package com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.api.AuthApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.LoginRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.LoginResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.RefreshTokenRequest;
import com.syttech.syttech.scheduler.scheduler.ports.in.LoginUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.RefreshTokenUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada (REST) para a API de autenticação.
 *
 * <p>Implementa a interface {@code AuthApi} gerada a partir do contrato OpenAPI ({@code
 * contract/input/auth-api.yaml}) e expõe os endpoints:
 *
 * <ul>
 *   <li>{@code POST /api/v1/auth/login} — autentica o cliente com e-mail + senha e devolve o par
 *       access/refresh token.
 *   <li>{@code POST /api/v1/auth/refresh} — troca um refresh token válido por um novo par
 *       access/refresh.
 * </ul>
 *
 * <p>Esta classe não contém regra de negócio: apenas delega para os casos de uso correspondentes
 * ({@link LoginUseCase} e {@link RefreshTokenUseCase}) e converte DTO ↔ domínio através do {@link
 * AuthMapper}. Erros de autenticação são traduzidos para {@code 401 application/problem+json} pelo
 * {@code GlobalExceptionHandler}.
 */
@RestController
public class AuthController implements AuthApi {

    /** Caso de uso responsável por autenticar credenciais e emitir tokens. */
    private final LoginUseCase login;

    /** Caso de uso responsável por validar o refresh token e reemitir o par de tokens. */
    private final RefreshTokenUseCase refresh;

    /**
     * Construtor com injeção de dependências dos casos de uso.
     *
     * @param login caso de uso de autenticação por credenciais
     * @param refresh caso de uso de renovação de tokens
     */
    public AuthController(final LoginUseCase login, final RefreshTokenUseCase refresh) {
        this.login = login;
        this.refresh = refresh;
    }

    /**
     * Autentica o cliente a partir de e-mail e senha.
     *
     * @param body payload validado com e-mail e senha
     * @return {@code 200 OK} com o par de tokens e os dados básicos do cliente
     */
    @Override
    public ResponseEntity<LoginResponse> login(final LoginRequest body) {
        return ResponseEntity.ok(AuthMapper.toResponse(login.login(AuthMapper.toCommand(body))));
    }

    /**
     * Renova o access token a partir de um refresh token válido.
     *
     * <p>Quando o refresh token está expirado, é inválido ou pertence a um cliente que não existe
     * mais, o caso de uso lança {@code UnauthenticatedException}, mapeada para {@code 401}.
     *
     * @param body payload contendo o refresh token previamente emitido
     * @return {@code 200 OK} com um novo par access/refresh
     */
    @Override
    public ResponseEntity<LoginResponse> refreshToken(final RefreshTokenRequest body) {
        return ResponseEntity.ok(
                AuthMapper.toResponse(refresh.refresh(AuthMapper.toCommand(body))));
    }
}
