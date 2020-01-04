package com.lwl.client.jdk;

import lombok.Data;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class Msg {
    /**
     * 请求的唯一标志，测试时用线程名称
     */
    private String requestId;
    /**
     * 请求类型，1加锁，2解锁
     */
    private String type;
    /**
     * 对哪个资源加锁
     */
    private String key;
    /**
     * 操作是否成功，用于服务端响应
     */
    private String success;
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
