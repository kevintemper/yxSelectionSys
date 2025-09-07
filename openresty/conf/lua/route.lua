-- 根据请求的环境进行目标URL选择
local env = ngx.var.http_x_env
local target_url

if env == "gray" then
    target_url = "http://gray_backend_url"
elseif env == "prd" then
    target_url = "http://prd_backend_url"
else
    target_url = "http://default_backend_url"
end

ngx.var.target_url = target_url
