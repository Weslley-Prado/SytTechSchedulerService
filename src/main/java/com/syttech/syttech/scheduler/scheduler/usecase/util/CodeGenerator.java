package com.syttech.syttech.scheduler.scheduler.usecase.util;

import java.security.SecureRandom;
import java.util.UUID;

/** Generates short, human-friendly codes for appointments and verification tokens. */
public final class CodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RNG = new SecureRandom();

    private CodeGenerator() {}

    /** Returns a code like "ABCD-2345" (8 chars + dash). */
    public static String shortAppointmentCode() {
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(ALPHABET.charAt(RNG.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /** Opaque token used for e-mail verification (resend/forgot flows). */
    public static String verificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
