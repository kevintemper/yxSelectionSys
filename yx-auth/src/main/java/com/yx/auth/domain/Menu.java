package com.yx.auth.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Menu implements Serializable {

  private UUID id;
  private String name;
  private String path;
  private String icon;
  private List<Resource> resources = new ArrayList<>();

  public Menu() {
  }

  public Menu(UUID id, String name, String path, String icon) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.icon = icon;
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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public void setResources(List<Resource> resources) {
    this.resources = resources;
  }
}
