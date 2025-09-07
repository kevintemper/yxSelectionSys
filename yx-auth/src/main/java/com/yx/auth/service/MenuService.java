package com.yx.auth.service;
import com.yx.auth.domain.*; import com.yx.auth.repo.*; import org.apache.poi.ss.usermodel.*; import org.springframework.stereotype.Service; import org.springframework.web.multipart.MultipartFile; import java.util.*;
@Service public class MenuService{
 private final MenuRepo menus; public MenuService(MenuRepo m){ menus=m; }
 public int importExcel(MultipartFile file){ int count=0; try(Workbook wb=org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())){
  var s=wb.getSheetAt(0); Map<String,Long> l1=new HashMap<>(), l2=new HashMap<>(); for(int i=1;i<=s.getLastRowNum();i++){ var r=s.getRow(i); if(r==null) continue;
   String a=val(r,0),b=val(r,1),c=val(r,2); if((a==null||a.isBlank())&&(b==null||b.isBlank())&&(c==null||c.isBlank())) continue;
   Long p1=l1.computeIfAbsent(a,k->save(null,a,1)); if(b!=null&&!b.isBlank()){ Long p2=l2.computeIfAbsent(a+"|"+b,k->save(p1,b,2)); if(c!=null&&!c.isBlank()){ save(p2,c,3); count++; } } else count++; } }catch(Exception e){ throw new RuntimeException(e);} return count; }
 private Long save(Long pid,String name,int lv){ var m=new Menu(); m.setParentId(pid); m.setName(name); m.setLevel(lv); return menus.save(m).getId(); }
 private String val(Row r,int i){ var c=r.getCell(i); return c==null?null:c.toString().trim(); }
 public java.util.List<java.util.Map<String,Object>> tree(){ return children(null); }
 private java.util.List<java.util.Map<String,Object>> children(Long pid){ var list=menus.findByParentIdOrderBySortAsc(pid); java.util.List<java.util.Map<String,Object>> ret=new java.util.ArrayList<>();
  for(var m:list){ var node=new java.util.LinkedHashMap<String,Object>(); node.put("id",m.getId()); node.put("title",m.getName()); node.put("path",m.getPath()); node.put("icon",m.getIcon()); node.put("children",children(m.getId())); ret.add(node);} return ret; }
}