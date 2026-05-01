package com.bac.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
    String baseUrl,
    String realm,
    Admin admin) {

  public record Admin(String username, String password) {
  }
}
