package com.yx.auth.domain;

import com.yx.auth.domain.enums.ResourceType;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Resource implements Serializable {

  private UUID id;
  private String name;
  private String identifier;
  private ResourceType type;

  public Resource() {
  }

  public Resource(UUID id, String name, String identifier, ResourceType type) {
    this.id = id;
    this.name = name;
    this.identifier = identifier;
    this.type = type;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public ResourceType getType() {
    return type;
  }

  public void setType(ResourceType type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Resource resource = (Resource) o;
    return Objects.equals(id, resource.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
