package com.yx.common.api;
import java.util.List;
public record PageResp<T>(long total, List<T> records){}
