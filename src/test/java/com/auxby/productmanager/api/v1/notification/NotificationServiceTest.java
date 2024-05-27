package com.auxby.productmanager.api.v1.notification;

import com.auxby.productmanager.api.v1.notification.repository.NotificationRepository;
import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.config.security.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks
    private NotificationService service;
    @Mock
    private NotificationRepository repository;

    @BeforeAll
    public static void mockApplicationUser() {
        User user = new User(1, "joe.doe@gmail.com", "pass4Test.", "joe.doe@gmail.com", Role.USER, true);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
    }

    @Test
    void should_DeleteOfferRelatedNotifications_When_MethodIsCalled() {
        //Act
        service.deleteOfferNotifications(1);

        //Assert
        var notificationTypesForDelete = List.of(
                "AUCTION_INTERRUPTED", "AUCTION_ENDED", "BID_EXCEEDED", "AUCTION_WON"
        );
        ArgumentCaptor<Integer> userIdArg = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<List<String>> notificationTypesArg = ArgumentCaptor.forClass(List.class);
        assertAll(
                () -> verify(repository, times(1)).deleteAllByOfferIdAndUserIdAndTypeIsIn(anyInt(), userIdArg.capture(), notificationTypesArg.capture()),
                () -> assertTrue(notificationTypesArg.getValue().containsAll(notificationTypesForDelete))
        );
    }
}