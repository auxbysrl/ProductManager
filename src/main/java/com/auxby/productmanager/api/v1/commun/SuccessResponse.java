package com.auxby.productmanager.api.v1.commun;

import lombok.Data;

@Data
public class SuccessResponse {
    private String message;

    public SuccessResponse() {
        this.message = "Success";
    }
}
