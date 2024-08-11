package com.auxby.productmanager.api.v1.bid;

import com.auxby.productmanager.api.v1.bid.model.BidSummary;
import com.auxby.productmanager.api.v1.bid.model.PlaceBidRequest;
import com.auxby.productmanager.exception.ActionNotAllowException;
import com.auxby.productmanager.exception.BidDeclinedException;
import com.auxby.productmanager.exception.InsufficientCoinsException;
import com.auxby.productmanager.exception.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.auxby.productmanager.utils.constant.AppConstant.BASE_V1_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BidController.class})
@ContextConfiguration(classes = {BidController.class, GlobalExceptionHandler.class})
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BidService bidService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    @WithMockUser
    void should_CreateNewBid_When_EndpointCalled() {
        //Arrange
        var request = new PlaceBidRequest(1, BigDecimal.valueOf(100), 5);

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/bid")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ThrowException_When_CreateBidThrowActionNotAllowed() {
        //Arrange
        var request = new PlaceBidRequest(1, BigDecimal.valueOf(100), 5);
        when(bidService.createBidForOnAuctionOffer(any())).thenThrow(new ActionNotAllowException("New bid not accepted."));

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/bid")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().is(481));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ThrowException_When_CreateBidThrowBidDeclined() {
        //Arrange
        var request = new PlaceBidRequest(1, BigDecimal.valueOf(100), 5);
        when(bidService.createBidForOnAuctionOffer(any()))
                .thenThrow(new BidDeclinedException("New bid not accepted.", Set.of(new BidSummary("Sam Smith", BigDecimal.valueOf(500)))));

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/bid")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().is(280));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ThrowException_When_CreateBidThrowNotEnoughCoins() {
        //Arrange
        var request = new PlaceBidRequest(1, BigDecimal.valueOf(100), 5);
        when(bidService.createBidForOnAuctionOffer(any()))
                .thenThrow(new InsufficientCoinsException("Place bid."));

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/bid")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().is(480));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnAllBids_When_EndpointIsCalled() {
        //Arrange
        when(bidService.getUserBids()).thenReturn(List.of());

        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/my-bids/offers"))
                .andExpect(status().isOk());
    }
}