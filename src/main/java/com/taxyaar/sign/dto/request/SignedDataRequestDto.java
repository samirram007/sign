package com.taxyaar.sign.dto.request;

public class SignedDataRequestDto {
    private String dataToSign;
    private String eriUserId;

    public String getDataToSign() {
        return dataToSign;
    }

    public void setDataToSign(String dataToSign) {
        this.dataToSign = dataToSign;
    }

    public String getEriUserId() {
        return eriUserId;
    }

    public void setEriUserId(String eriUserId) {
        this.eriUserId = eriUserId;
    }
}
