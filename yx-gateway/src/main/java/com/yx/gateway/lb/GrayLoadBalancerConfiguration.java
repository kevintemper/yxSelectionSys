package com.yx.gateway.lb;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class GrayLoadBalancerConfiguration {

    @Bean
    ReactorServiceInstanceLoadBalancer grayLoadBalancer(Environment env,
                                                        LoadBalancerClientFactory factory) {
        String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        ObjectProvider<ServiceInstanceListSupplier> supplier =
                factory.getLazyProvider(name, ServiceInstanceListSupplier.class);
        return new GrayLoadBalancer(supplier, name);
    }
}
