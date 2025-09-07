package com.yx.common.web;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
  record Err(String code,String message){}
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> bad(IllegalArgumentException e){
    return ResponseEntity.badRequest().body(new Err("BAD_REQUEST", e.getMessage()));
  }
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<?> re(RuntimeException e){
    return ResponseEntity.status(500).body(new Err("SERVER_ERROR", e.getMessage()));
  }
}
