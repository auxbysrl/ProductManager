package com.auxby.productmanager.utils.constant;

import lombok.Getter;

@Getter
public enum CustomHttpStatus {
    INSUFFICIENT_COINS(480, "Insufficient coins."),
    ACTION_NOT_ALLOW(481, "Action not allow."),
    FAIL_TO_GENERATE_LINK(482, "Link Generation failed."),
    BID_NOT_ACCEPTED(280, "Placed bid not accepted.");

    CustomHttpStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    private final int value;
    private final String reasonPhrase;
}
