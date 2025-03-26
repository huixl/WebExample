package com.lixh.webexample.adapter.dto;

import com.alibaba.fastjson.JSONObject;

public interface StreamCallback {

    void onData(JSONObject data);

    void onError(Exception e);

    void onComplete();
}