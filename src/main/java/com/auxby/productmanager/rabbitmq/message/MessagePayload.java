package com.auxby.productmanager.rabbitmq.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePayload implements Serializable {
    private String receiver;
    private Integer offerId;
    private MessageType messageType;
    private Map<String, String> messageExtraInfo;
}
