package com.auxby.productmanager.rabbitmq.message;

import lombok.Getter;

@Getter
public enum MessageType {
    BID_EXCEEDED("You're bid for ${OFFER_NAME} was exceeded.", "Bid exceeded."),
    NEW_CHAT_STARTED("User ${USER_NAME} started new chat with you.", "New chat started."),
    NEW_MESSAGE_RECEIVED("You received a new message from ${USER_NAME}.", "New message received."),
    AUCTION_WON("You won auction for ${OFFER_NAME}.", "Auction won."),
    AUCTION_ENDED("The auction for ${OFFER_NAME} has ended.", "Auction ended."),
    AUCTION_INTERRUPTED("The auction for ${OFFER_NAME} has ended by it's owner.", "Auction ended."),
    ACTION_FAILED("Fail to perform action ${ACTION}", "Action failed."),
    OFFER_EXPIRATION("Offer ${OFFER_NAME} is about to expire.", "Offer is about to expire.");

    private final String messageTemplate;
    private final String title;

    MessageType(String messageTemplate, String title) {
        this.messageTemplate = messageTemplate;
        this.title = title;
    }
}
