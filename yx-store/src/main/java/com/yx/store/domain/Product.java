package com.yx.store.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class Product implements Serializable {

  private UUID id;
  @NotBlank(message = "商品名称不能为空")
  private String name;
  @NotNull(message = "价格不能为空")
  @DecimalMin(value = "0.00", message = "价格必须大于等于0")
  private BigDecimal price;
  private boolean gray;

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

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public boolean isGray() {
    return gray;
  }

  public void setGray(boolean gray) {
    this.gray = gray;
  }
}
