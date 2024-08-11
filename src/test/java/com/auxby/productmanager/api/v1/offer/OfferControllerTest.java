package com.auxby.productmanager.api.v1.offer;

import com.auxby.productmanager.api.v1.offer.model.ChangeStatusRequest;
import com.auxby.productmanager.api.v1.offer.model.OfferSummary;
import com.auxby.productmanager.api.v1.offer.model.PromoteInfo;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearch;
import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.config.security.Role;
import com.auxby.productmanager.exception.handler.GlobalExceptionHandler;
import com.auxby.productmanager.utils.SimplePage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.auxby.productmanager.utils.constant.AppConstant.BASE_V1_URL;
import static mock.OfferTestMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OfferController.class})
@ContextConfiguration(classes = {OfferController.class, GlobalExceptionHandler.class})
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferService offerService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnListOfAvailableProducts_When_OffersAvailableExits() {
        //Arrange
        SimplePage<OfferSummary> expectedPage = new SimplePage<>(List.of(mockOfferSummary()), 1L, 1, 1, 1, new ArrayList<>());
        when(offerService.getAllOffers(any(), any()))
                .thenReturn(expectedPage);

        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnProducts_When_OfferWithProvidedIdsExists() {
        //Arrange
        when(offerService.getOffersByIds(any()))
                .thenReturn(List.of(mockOfferSummary()));

        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/getByIds")
                        .param("id", "1", "2") // Simulating request param as '?id=1&id=2'
                )
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnOffer_When_OfferWithIdPresentInDB() {
        //Arrange
        when(offerService.getOfferById(any(Integer.class), any(Boolean.class)))
                .thenReturn(mockDetailedOffer());

        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/1")
                        .param("increaseView", String.valueOf(true)))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnAllUserOffers_When_UserHasActiveOffers() {
        //Arrange
        User mockUser = new User(1, "joe.doe@gmail.com", UUID.randomUUID().toString(), "joe.doe@gmail.com", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                mockUser.getAuthorities()
        );

        when(offerService.getAllUserOffers(any(String.class)))
                .thenReturn(List.of(mockOfferSummary()));

        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/user/my-offers")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_CallGetOfferCategories_When_EndpointIsCalled() {
        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/category"))
                .andExpect(status().isOk());
        verify(offerService, times(1)).getOfferCategory();
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_CallGetFavoriteOffers_When_EndpointIsCalled() {
        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/favorites"))
                .andExpect(status().isOk());
        verify(offerService, times(1)).getFavoriteOffers();
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_CallGetPromotedOffers_When_EndpointIsCalled() {
        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/promoted"))
                .andExpect(status().isOk());
        verify(offerService, times(1)).getPromotedOffers();
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnOffersInSpecificCategory_When_EndpointIsCalled() {
        //Act && Assert
        mockMvc.perform(get("/" + BASE_V1_URL + "/search-summary")
                        .param("offerTitle", OFFER_TITLE))
                .andExpect(status().isOk());
        ArgumentCaptor<String> titleArg = ArgumentCaptor.forClass(String.class);
        assertAll(
                () -> verify(offerService, times(1)).searchOfferInCategories(titleArg.capture()),
                () -> assertEquals(OFFER_TITLE, titleArg.getValue())
        );
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_FailAddNewOffer_When_RequestIsEmpty() {
        //Arrange
        var request = "{}";

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ReturnEmptyList_When_NoOffersFoundBasedOnSearchResult() {
        //Arrange
        var searchRequest = new OfferSearch(OFFER_CATEGORIES, null, OFFER_TITLE, null, null, null, null);

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/search")
                        .content(mapper.writeValueAsString(searchRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<OfferSearch> searchArg = ArgumentCaptor.forClass(OfferSearch.class);

        assertAll(
                () -> verify(offerService, times(1)).advancedSearch(searchArg.capture()),
                () -> assertEquals(OFFER_TITLE, searchArg.getValue().title()),
                () -> assertTrue(searchArg.getValue().categories().containsAll(OFFER_CATEGORIES))
        );
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_SetOfferAsFavorite_When_EndpointIsCalled() {
        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/1/favorite")
                        .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<Integer> idArg = ArgumentCaptor.forClass(Integer.class);
        verify(offerService, times(1)).addRemoveFavoriteOffer(idArg.capture());
        assertEquals(1, idArg.getValue());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_PromoteOffer_When_EndpointIsCalled() {
        //Arrange
        var request = new PromoteInfo(5, new Date());

        //Act && Assert
        mockMvc.perform(post("/" + BASE_V1_URL + "/1/promote")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<Integer> idArg = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PromoteInfo> promoteArg = ArgumentCaptor.forClass(PromoteInfo.class);
        verify(offerService, times(1)).promoteOffer(idArg.capture(), promoteArg.capture());
        assertEquals(1, idArg.getValue());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_ChangeOfferStatus_When_EndpointIsCalled() {
        //Arrange
        var request = new ChangeStatusRequest(5);
        User mockUser = new User(1, "joe.doe@gmail.com", UUID.randomUUID().toString(), "joe.doe@gmail.com", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                mockUser.getAuthorities()
        );

        //Act && Assert
        mockMvc.perform(put("/" + BASE_V1_URL + "/1/changeStatus")
                        .content(mapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void should_DeleteOffer_When_EndpointIsCalled() {
        //Arrange
        User mockUser = new User(1, "joe.doe@gmail.com", UUID.randomUUID().toString(), "joe.doe@gmail.com", Role.USER, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                mockUser.getAuthorities()
        );

        //Act && Assert
        mockMvc.perform(delete("/" + BASE_V1_URL + "/1")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk());
        verify(offerService, times(1)).deleteOffer(any(), anyString());
    }
}