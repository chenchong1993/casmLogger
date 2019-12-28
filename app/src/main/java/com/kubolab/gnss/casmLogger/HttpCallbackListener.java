package com.kubolab.gnss.casmLogger;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
