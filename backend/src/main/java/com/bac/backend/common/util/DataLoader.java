package com.bac.backend.common.util;

import com.bac.backend.auth.AppUser;
import com.bac.backend.auth.AppUserRepository;
import com.bac.backend.auth.Role;
import com.bac.backend.auth.RoleRepository;
import com.bac.backend.common.security.KeycloakProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Profile("!test")
public class DataLoader implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

  private static final List<String[]> SYSTEM_ROLES = List.of(
      new String[] { "SYSTEM_ADMIN", "System Administrator" },
      new String[] { "OFFICE_MANAGER", "Office Manager" },
      new String[] { "FINANCE_ADMIN", "Finance Administrator" },
      new String[] { "DSP", "Direct Support Professional" },
      new String[] { "AUDIT_INSPECTOR", "Audit Inspector" });

  private static final String ADMIN_EMAIL = "admin@bac.com";
  private static final String ADMIN_PASSWORD = "Admin@123456";

  private final RoleRepository roleRepository;
  private final AppUserRepository appUserRepository;
  private final KeycloakProperties props;
  private final RestClient restClient;

  public DataLoader(RoleRepository roleRepository,
      AppUserRepository appUserRepository,
      KeycloakProperties props) {
    this.roleRepository = roleRepository;
    this.appUserRepository = appUserRepository;
    this.props = props;
    this.restClient = RestClient.create();
  }

  @Override
  public void run(ApplicationArguments args) {
    seedRoles();
    seedAdminUser();
  }

  private void seedRoles() {
    Set<String> seededRoles = new HashSet<>();
    for (String[] r : SYSTEM_ROLES) {
      String code = r[0];
      String name = r[1];
      if (roleRepository.findByCode(code).isEmpty()) {
        roleRepository.save(Role.builder().code(code).name(name).system(true).build());
        seededRoles.add(code);
      }
    }
    if (seededRoles.size() != 0)
      log.info("Seeded {} roles: {}", seededRoles.size(), String.join(", ", seededRoles));
  }

  private void seedAdminUser() {
    try {
      String adminToken = getAdminToken();
      String keycloakUserId = findOrCreateKeycloakUser(adminToken);
      if (appUserRepository.findByKeycloakSub(keycloakUserId).isEmpty()) {
        Role systemAdmin = roleRepository.findByCode("SYSTEM_ADMIN")
            .orElseThrow(() -> new IllegalStateException("SYSTEM_ADMIN role not found"));
        AppUser user = new AppUser();
        user.setKeycloakSub(keycloakUserId);
        user.setEmail("admin@bac.com");
        user.setRole(systemAdmin);
        user.setActive(true);
        appUserRepository.save(user);
        log.info("Admin user seeded: {}", "admin@bac.com");
      }
    } catch (Exception e) {
      log.warn("Keycloak admin seeding skipped — Keycloak may not be ready: {}", e.getMessage());
    }
  }

  private String getAdminToken() {
    var form = new LinkedMultiValueMap<String, String>();
    form.add("grant_type", "password");
    form.add("client_id", "admin-cli");
    form.add("username", props.admin().username());
    form.add("password", props.admin().password());

    @SuppressWarnings("unchecked")
    Map<String, Object> response = restClient.post()
        .uri(props.baseUrl() + "/realms/master/protocol/openid-connect/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(Map.class);

    return (String) response.get("access_token");
  }

  private String findOrCreateKeycloakUser(String adminToken) {
    String searchUrl = props.baseUrl() + "/admin/realms/" + props.realm()
        + "/users?search=" + ADMIN_EMAIL;

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> existing = restClient.get()
        .uri(searchUrl)
        .header("Authorization", "Bearer " + adminToken)
        .retrieve()
        .body(List.class);

    if (existing != null && !existing.isEmpty()) {
      return (String) existing.getFirst().get("id");
    }

    return createKeycloakUser(adminToken);
  }

  private String createKeycloakUser(String adminToken) {
    String usersUrl = props.baseUrl() + "/admin/realms/" + props.realm() + "/users";

    Map<String, Object> userBody = Map.of(
        "username", ADMIN_EMAIL,
        "email", ADMIN_EMAIL,
        "enabled", true,
        "emailVerified", true);

    var response = restClient.post()
        .uri(usersUrl)
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .body(userBody)
        .retrieve()
        .toBodilessEntity();

    URI location = response.getHeaders().getLocation();
    if (location == null) {
      throw new IllegalStateException("Keycloak did not return a Location header for the created user");
    }
    String path = location.getPath();
    String userId = path.substring(path.lastIndexOf('/') + 1);

    setPassword(adminToken, userId);
    assignRealmRole(adminToken, userId, "SYSTEM_ADMIN");

    return userId;
  }

  private void setPassword(String adminToken, String userId) {
    restClient.put()
        .uri(props.baseUrl() + "/admin/realms/" + props.realm()
            + "/users/" + userId + "/reset-password")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("type", "password", "value", ADMIN_PASSWORD, "temporary", false))
        .retrieve()
        .toBodilessEntity();
  }

  private void assignRealmRole(String adminToken, String userId, String roleName) {
    @SuppressWarnings("unchecked")
    Map<String, Object> role = restClient.get()
        .uri(props.baseUrl() + "/admin/realms/" + props.realm() + "/roles/" + roleName)
        .header("Authorization", "Bearer " + adminToken)
        .retrieve()
        .body(Map.class);

    restClient.post()
        .uri(props.baseUrl() + "/admin/realms/" + props.realm()
            + "/users/" + userId + "/role-mappings/realm")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .body(List.of(role))
        .retrieve()
        .toBodilessEntity();
  }
}
