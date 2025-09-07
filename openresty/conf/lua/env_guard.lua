-- conf/lua/env_guard.lua
-- 作用：把前端的环境标记（来自 Cookie）写入请求头 X-Env，并禁止跨环境伪造

local ck = require "resty.cookie"

-- 读取 Cookie 中的 env（gray 或 prd），默认 prd
local cookie, err = ck:new()
local env = "prd"
if cookie then
  local v, e = cookie:get("env")
  if v == "gray" or v == "prd" then
    env = v
  end
end

-- 强制把 X-Env 写成 Cookie 中的环境（防止客户端伪造）
ngx.req.clear_header("X-Env")
ngx.req.set_header("X-Env", env)

-- 如果是灰度前端（env=gray），禁止任何把 env 改成 prd 的小动作（比如某些接口参数想越权）
-- 这里示例简单拦：如果 query/body 里带 __force_env=prd，就拒绝
local args = ngx.req.get_uri_args()
if env == "gray" and (args["__force_env"] == "prd") then
  ngx.status = ngx.HTTP_FORBIDDEN
  ngx.say("Forbidden: gray frontend can only call gray backend.")
  return ngx.exit(ngx.HTTP_FORBIDDEN)
end

-- 也可以在响应头里打一个调试头，方便你在浏览器 Network 面板确认最终的 env
ngx.header["X-Env-Echo"] = env
