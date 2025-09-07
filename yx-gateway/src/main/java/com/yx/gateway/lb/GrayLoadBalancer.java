package com.yx.gateway.lb;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final AtomicInteger position;
    private final ObjectProvider<ServiceInstanceListSupplier> supplierProvider;

    public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> supplierProvider, String serviceId) {
        this(supplierProvider, serviceId, new AtomicInteger(0));
    }

    public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> supplierProvider, String serviceId, AtomicInteger position) {
        this.supplierProvider = supplierProvider;
        this.serviceId = serviceId;
        this.position = position;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = supplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);

        String env = "prd";
        if (request instanceof RequestDataContext rdc) {
            HttpHeaders headers = rdc.getClientRequest().getHeaders();
            String v = headers.getFirst("X-Env");
            if (v != null) env = v;
        }

        final String finalEnv = env;
        return supplier.get().next().map(instances -> {
            List<ServiceInstance> candidates = instances.stream()
                    .filter(si -> finalEnv.equalsIgnoreCase(si.getMetadata().getOrDefault("env", "prd")))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                candidates = instances; // 兜底
            }
            if (candidates.isEmpty()) {
                return new EmptyResponse();
            }
            // 简单轮询
            int pos = Math.abs(this.position.incrementAndGet());
            ServiceInstance chosen = candidates.get(pos % candidates.size());
            return new DefaultResponse(chosen);
        });
    }
}
