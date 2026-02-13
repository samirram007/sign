package com.taxyaar.sign.dto.request;

public class LoginRequestDto {
    private String sign;
    private String data;
    private String eriUserId;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEriUserId() {
        return eriUserId;
    }

    public void setEriUserId(String eriUserId) {
        this.eriUserId = eriUserId;
    }
}
