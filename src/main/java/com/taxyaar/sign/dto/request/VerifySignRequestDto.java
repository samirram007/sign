package com.taxyaar.sign.dto.request;

public class VerifySignRequestDto {

    private String sign; // Base64 CMS signed data
    private String data; // Base64 original data

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
}
