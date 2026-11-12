package com.reservas.security;

import java.util.List;
import java.util.UUID;

public record JwtClaims(UUID userId, String email, List<String> roles) {
}
