package com.taxyaar.sign.dto.response;

public class VerifySignResponseDto {

    private boolean valid;

    public VerifySignResponseDto(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
