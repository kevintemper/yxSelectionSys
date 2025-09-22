package com.yx.store.service.impl;

import com.yx.store.domain.Product;
import com.yx.store.domain.Store;
import com.yx.store.service.StoreService;
import java.time.Clock;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl implements StoreService {

  private final Clock clock;
  private final Map<UUID, Store> stores = new ConcurrentHashMap<>();

  public StoreServiceImpl(Clock clock) {
    this.clock = clock;
    bootstrap();
  }

  private void bootstrap() {
    Store flagship = new Store();
    flagship.setId(UUID.randomUUID());
    flagship.setName("严选旗舰店");
    flagship.setOwnerId("admin");
    flagship.setDescription("严选核心商品灰度示例");
    flagship.setCreatedAt(clock.instant());
    var grayProduct = new Product();
    grayProduct.setId(UUID.randomUUID());
    grayProduct.setName("灰度精品咖啡");
    grayProduct.setPrice(BigDecimal.valueOf(29.9));
    grayProduct.setGray(true);
    flagship.getProducts().add(grayProduct);
    var prodProduct = new Product();
    prodProduct.setId(UUID.randomUUID());
    prodProduct.setName("严选手冲壶");
    prodProduct.setPrice(BigDecimal.valueOf(199));
    prodProduct.setGray(false);
    flagship.getProducts().add(prodProduct);
    stores.put(flagship.getId(), flagship);
  }

  @Override
  public Store create(Store store) {
    store.setId(UUID.randomUUID());
    store.setCreatedAt(clock.instant());
    Store toSave = copyStore(store);
    stores.put(toSave.getId(), toSave);
    return copyStore(toSave);
  }

  @Override
  public Store update(Store store) {
    if (store.getId() == null || !stores.containsKey(store.getId())) {
      throw new IllegalArgumentException("店铺不存在");
    }
    Store existing = stores.get(store.getId());
    existing.setName(store.getName());
    existing.setDescription(store.getDescription());
    existing.setOwnerId(store.getOwnerId());
    List<Product> copies = new ArrayList<>();
    if (store.getProducts() != null) {
      store.getProducts().forEach(product -> {
        Product copy = new Product();
        copy.setId(product.getId() != null ? product.getId() : UUID.randomUUID());
        copy.setName(product.getName());
        copy.setPrice(product.getPrice());
        copy.setGray(product.isGray());
        copies.add(copy);
      });
    }
    existing.setProducts(copies);
    return existing;
  }

  @Override
  public List<Store> list() {
    return stores.values().stream().map(this::copyStore).toList();
  }

  @Override
  public Optional<Store> findById(UUID id) {
    return Optional.ofNullable(stores.get(id));
  }

  @Override
  public void delete(UUID id) {
    stores.remove(id);
  }

  private Store copyStore(Store source) {
    Store target = new Store();
    target.setId(source.getId());
    target.setName(source.getName());
    target.setOwnerId(source.getOwnerId());
    target.setDescription(source.getDescription());
    target.setCreatedAt(source.getCreatedAt());
    if (source.getProducts() != null) {
      source.getProducts().forEach(product -> {
        Product copy = new Product();
        copy.setId(product.getId() != null ? product.getId() : UUID.randomUUID());
        copy.setName(product.getName());
        copy.setPrice(product.getPrice());
        copy.setGray(product.isGray());
        target.getProducts().add(copy);
      });
    }
    return target;
  }
}
