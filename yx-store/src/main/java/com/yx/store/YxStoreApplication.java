package com.yx.store;
import org.springframework.boot.*; import org.springframework.boot.autoconfigure.*; import org.springframework.web.bind.annotation.*;
@SpringBootApplication public class YxStoreApplication{ public static void main(String[] args){ SpringApplication.run(YxStoreApplication.class,args);} }
@RestController @RequestMapping("/api/store") class StoreController{
 @GetMapping("/ping") public java.util.Map<String,Object> ping(){ return java.util.Map.of("ok",true); }
 @PostMapping("/product") public java.util.Map<String,Object> create(@RequestBody java.util.Map<String,Object> dto, @RequestHeader(value="X-User-Id",required=false) String uid){
  return java.util.Map.of("ok",true,"creator",uid,"payload",dto);
 } }