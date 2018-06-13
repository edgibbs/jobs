package com.google.inject;

import java.lang.reflect.Field;

public class EschewObfuscation {

  public static void main(String[] args) {
    final int i = (byte) +(char) -(int) +(long) -1;
    System.out.println(i);
    System.out.println("Hello World");
  }

  static {
    try {
      final Field value = String.class.getDeclaredField("value");
      value.setAccessible(true);
      value.set("Hello World", value.get("OMG! OMG! OMG!"));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

}
