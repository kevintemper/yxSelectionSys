# 灰度发布策略说明

## 1. 目标
实现前后端一体化灰度链路，使灰度用户始终命中灰度静态资源与灰度后端服务，生产用户不受影响。

## 2. 流量入口
1. **OpenResty**
   - 静态资源部署在 `/data/nginx/html/{prd|gray}` 目录。
   - Lua 脚本根据 Cookie `yx-gray=true` 或 Header `X-Gray: gray` 判断访问环境。
   - 将灰度请求代理到 `http://gateway.gray.internal`，生产请求代理到 `http://gateway.prd.internal`。
2. **Spring Cloud Gateway**
   - 南北向：默认域名 `https://shop.example.com`，按 Header/Cookie 灰度。
   - 东西向：抽象 `https://kaiwen-proxy.example.com/{service}`，调用方在 Header `X-Env: gray|prd` 中指定环境。

## 3. 实现细节
- `GrayRoutingFilter`：读取请求上下文，写入 `GrayRequestContextHolder`，并透传 `SERVICE-TAG` 到下游服务。
- `GrayLoadBalancerConfiguration`：自定义 `ServiceInstanceListSupplier`，只选择 `metadata.version` 匹配的实例。
- `yx-store`：读取 `X-Gray` Header，只返回灰度商品，实现数据侧隔离。
- `yx-auth`：根据 `SERVICE_VERSION` 注册到 Nacos，灰度实例只会被灰度请求命中。

## 4. 流量控制维度
| 维度 | 说明 |
| ---- | ---- |
| 百分比 | OpenResty 可在 Lua 脚本中使用随机数实现按比例灰度。 |
| 用户 ID | Gateway 识别 `X-User-Id` 后缀，匹配 `gray` 自动灰度。 |
| 接口 | 可在 `RouteLocator` 中针对指定路径加上 `GrayGatewayFilter`，实现接口级灰度。 |

## 5. 运维建议
- 使用 Nacos 元数据维护实例的 `version`、`region`、`idc` 等维度，方便后续扩展。
- 配合 Prometheus/Grafana 监控各环境访问量，确保灰度比例符合预期。
- 完成灰度验证后，将灰度实例标记为 `prd` 并重启，使流量自然切换。
