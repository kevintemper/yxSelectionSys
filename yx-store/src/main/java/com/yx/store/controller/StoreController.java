package com.yx.store.controller;

import com.yx.store.domain.Store;
import com.yx.store.service.StoreService;
import com.yx.common.core.model.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

  private final StoreService storeService;

  public StoreController(StoreService storeService) {
    this.storeService = storeService;
  }

  @GetMapping
  public ApiResponse<List<Store>> list(@RequestHeader(value = "X-Gray", required = false) String grayFlag) {
    List<Store> stores = storeService.list();
    if ("true".equalsIgnoreCase(grayFlag)) {
      stores.forEach(store -> store.getProducts().removeIf(product -> !product.isGray()));
    }
    return ApiResponse.success(stores);
  }

  @PostMapping
  public ApiResponse<Store> create(@Valid @RequestBody Store store) {
    return ApiResponse.success(storeService.create(store));
  }

  @PutMapping
  public ApiResponse<Store> update(@Valid @RequestBody Store store) {
    return ApiResponse.success(storeService.update(store));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Store>> detail(@PathVariable UUID id) {
    return storeService.findById(id)
        .map(store -> ResponseEntity.ok(ApiResponse.success(store)))
        .orElseGet(() -> ResponseEntity.status(404)
            .body(ApiResponse.failure(org.springframework.http.HttpStatus.NOT_FOUND, "店铺不存在")));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable UUID id) {
    storeService.delete(id);
    return ApiResponse.success(null);
  }
}
