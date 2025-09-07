package com.yx.auth.api;
import com.yx.auth.service.MenuService; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.util.Map;
@RestController @RequestMapping("/api/menu") public class MenuController{
 private final MenuService svc; public MenuController(MenuService s){ svc=s; }
 @PostMapping("/import") public Map<String,Integer> imp(@RequestParam("file") MultipartFile f){ return Map.of("imported",svc.importExcel(f)); }
 @GetMapping("/tree") public Object tree(){ return svc.tree(); }
}