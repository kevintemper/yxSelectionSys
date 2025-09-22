灰度发布策略说明
1. 目标
实现前后端一体化灰度链路，使灰度用户始终命中灰度静态资源与灰度后端服务，生产用户不受影响。

2. 流量入口
2.1 OpenResty
静态资源部署：静态资源部署在 openresty/sites/{prd|gray} 目录，map 指令会根据当前命中的环境选择根目录。

灰度策略：通过 map + split_clients 实现"显式指定优先 + 5% 金丝雀"策略：

Cookie(env=gray|prd)、Header(X-Env/X-Gray) 与用户 ID 后缀可强制命中灰度

其余请求走 5% 随机灰度

API 透传：location /api/ 与 location /kaiwen-proxy/ 统一透传 X-Env/X-Gray/SERVICE-TAG，保证后端负载与日志都能识别访问环境

网关路由：gateway_upstream 可根据 target_env 指向不同的 Gateway 实例，或根据需要改为直接打到微服务

2.2 Spring Cloud Gateway
南北向流量：默认域名 https://shop.example.com，按 Header/Cookie 灰度

东西向流量：抽象 https://kaiwen-proxy.example.com/{service}，调用方在 Header X-Env: gray|prd 中指定环境

3. 实现细节
3.1 网关层实现
GrayRoutingFilter：读取请求上下文，写入 GrayRequestContextHolder，并透传 SERVICE-TAG 到下游服务

GrayLoadBalancerConfiguration：自定义 ServiceInstanceListSupplier，只选择 metadata.version 匹配的实例

3.2 业务层实现
yx-store：读取 X-Gray Header，只返回灰度商品，实现数据侧隔离

yx-auth：根据 SERVICE_VERSION 注册到 Nacos，灰度实例只会被灰度请求命中

4. 流量控制维度
维度	说明	实现方式
百分比	按比例灰度（默认 5%）	OpenResty 使用 split_clients 实现
用户 ID	根据用户标识灰度	Gateway 识别 X-User-Id 后缀，匹配 gray 自动灰度
接口路由	接口级灰度控制	在 RouteLocator 中针对指定路径加上 GrayGatewayFilter
业务参数	基于业务逻辑的灰度	自定义灰度策略，如按地区、用户等级等
5. 配置示例
5.1 OpenResty 配置
nginx
# 灰度环境映射
map $cookie_env $target_env {
    default "prd";
    "gray" "gray";
    "prd" "prd";
}

# 5% 流量灰度
split_clients "${remote_addr}${http_user_agent}" $canary_env {
    5% "gray";
    95% "prd";
}

# 最终环境选择
map $target_env $final_env {
    "prd" "prd";
    "gray" "gray";
    default $canary_env;
}
5.2 Gateway 灰度过滤器
java
@Component
public class GrayRoutingFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 读取灰度标识
        String grayTag = extractGrayTag(exchange);
        GrayRequestContextHolder.setGrayTag(grayTag);
        
        // 透传到下游服务
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("SERVICE-TAG", grayTag)
            .build();
            
        return chain.filter(exchange.mutate().request(request).build());
    }
}