package com.yx.common.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record PageResponse<T>(List<T> records, long total, long page, long size) implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
}
