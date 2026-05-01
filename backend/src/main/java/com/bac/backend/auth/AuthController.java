package com.bac.backend.auth;

import com.bac.backend.auth.dto.UserProfileResponse;
import com.bac.backend.common.exception.ApplicationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

  private final AppUserRepository appUserRepository;

  public AuthController(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @GetMapping("/me")
  public UserProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
    log.info("Me endpoint called for user: {}", jwt.getSubject());
    AppUser user = appUserRepository.findByKeycloakSub(jwt.getSubject())
        .orElseThrow(() -> ApplicationException.notFound("User profile not found"));
    return new UserProfileResponse(user.getId(), user.getEmail(), user.getRole().getCode());
  }
}
