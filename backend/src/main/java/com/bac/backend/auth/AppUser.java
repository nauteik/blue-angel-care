package com.bac.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.bac.backend.common.persistence.BaseEntity;
import java.time.Instant;
import lombok.Builder;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AppUser extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String keycloakSub;

  @Column(columnDefinition = "citext", nullable = false, unique = true)
  private String email;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(name = "is_active")
  @Builder.Default
  private boolean active = true;

  private Instant lastLoginAt;

}
