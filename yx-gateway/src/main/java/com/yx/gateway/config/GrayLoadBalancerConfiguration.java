package com.yx.gateway.config;

import com.yx.gateway.filter.GrayRequestContextHolder;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

@Configuration
public class GrayLoadBalancerConfiguration {

  @Bean
  public ServiceInstanceListSupplier serviceInstanceListSupplier(ObjectProvider<ReactiveDiscoveryClient> discoveryClient,
      ObjectProvider<ServiceInstanceListSupplier> listSupplier, Environment environment) {
    ServiceInstanceListSupplier delegate = listSupplier.getIfAvailable();
    if (delegate == null && discoveryClient.getIfAvailable() != null) {
      delegate = ServiceInstanceListSupplier.builder().withDiscoveryClient(discoveryClient.getIfAvailable())
          .withEnvironment(environment)
          .build();
    }
    if (delegate == null) {
      throw new IllegalStateException("未能初始化 ServiceInstanceListSupplier");
    }
    return new ServiceInstanceListSupplier() {
      @Override
      public Flux<List<ServiceInstance>> get() {
        return delegate.get().map(this::filterByTag);
      }

      @Override
      public Flux<List<ServiceInstance>> get(org.springframework.cloud.client.loadbalancer.Request request) {
        return delegate.get(request).map(this::filterByTag);
      }

      @Override
      public String getServiceId() {
        return delegate.getServiceId();
      }

      private List<ServiceInstance> filterByTag(List<ServiceInstance> instances) {
        String tag = GrayRequestContextHolder.getTag();
        if (tag == null || tag.isBlank()) {
          return instances;
        }
        List<ServiceInstance> filtered = instances.stream()
            .filter(instance -> tag.equalsIgnoreCase(instance.getMetadata().getOrDefault("version", "prd")))
            .collect(Collectors.toList());
        return filtered.isEmpty() ? instances : filtered;
      }
    };
  }
}
