package com.syttech.syttech.scheduler.scheduler.domain.command;

/**
 * Comando de entrada do caso de uso de renovação de tokens.
 *
 * @param refreshToken refresh token JWT emitido anteriormente pelo serviço; deve estar não vazio,
 *     não expirado e pertencer a um cliente existente
 */
public record RefreshTokenCommand(String refreshToken) {}
