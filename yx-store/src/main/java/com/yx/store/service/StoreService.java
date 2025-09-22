package com.yx.store.service;

import com.yx.store.domain.Store;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreService {

  Store create(Store store);

  Store update(Store store);

  List<Store> list();

  Optional<Store> findById(UUID id);

  void delete(UUID id);
}
