package com.yx.gateway.filter;

public final class GrayRequestContextHolder {

  private static final ThreadLocal<String> TAG = new InheritableThreadLocal<>();

  private GrayRequestContextHolder() {
  }

  public static void setTag(String tag) {
    TAG.set(tag);
  }

  public static String getTag() {
    return TAG.get();
  }

  public static void clear() {
    TAG.remove();
  }
}
