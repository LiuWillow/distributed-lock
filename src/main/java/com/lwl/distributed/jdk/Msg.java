package com.lwl.distributed.jdk;

import lombok.Data;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class Msg {
    private String requestId;
    private String type;

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
