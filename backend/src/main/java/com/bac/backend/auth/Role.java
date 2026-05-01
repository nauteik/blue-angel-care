package com.bac.backend.auth;

import com.bac.backend.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

  @Column(columnDefinition = "citext", nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(name = "is_system", nullable = false)
  private boolean system;

}