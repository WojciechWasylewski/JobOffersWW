package com.joboffers.infrastructure.security.jwt;

import com.joboffers.infrastructure.loginandregister.controller.dto.JwtResponseDto;
import com.joboffers.infrastructure.loginandregister.controller.dto.TokenRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class JwtAthenticatorFacade {
    private final AuthenticationManager authenticationManager;
    public JwtResponseDto authenticateAndGenerateToken(TokenRequestDto tokenRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(tokenRequestDto.username(), tokenRequestDto.password()));
        return JwtResponseDto.builder().build();
    }
}
