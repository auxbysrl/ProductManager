package com.auxby.productmanager.api.v1.user;

import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final User USER = new User(1, "joe.doe@gmail.com", "pass4Test.", "joe.doe@gmail.com", Role.USER, true);

    @InjectMocks
    private UserService userService;
    @Mock
    private UserDetailsRepository userDetailsRepository;

    @BeforeAll
    public static void mockApplicationUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
    }

    @Test
    void should_ReturnUser_When_MethodIsCalled() {
        //Arrange
        when(userDetailsRepository.findUserDetailsByUsername(anyString()))
                .thenReturn(Optional.of(new UserDetails()));

        //Act
        var result = userService.getUser();

        //Assert
        assertNotNull(result);
    }

    @Test
    void should_UpdateUserCoinsNumber_When_MethodIsCalled() {
        //Act
        userService.updateUserCoinsNumber(USER.getUsername(), 10);

        //Assert
        ArgumentCaptor<String> usernameArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> coinsArg = ArgumentCaptor.forClass(Integer.class);

        verify(userDetailsRepository, times(1)).updateUserCoinsNumber(usernameArg.capture(), coinsArg.capture());
        assertEquals(USER.getUsername(), usernameArg.getValue());
        assertEquals(10, coinsArg.getValue());
    }

    @Test
    void should_UpdateLasSeenTime_When_MethodIsCalled() {
        //Act
        userService.updateUserLastSeen(USER.getUsername());

        //Assert
        ArgumentCaptor<String> usernameArg = ArgumentCaptor.forClass(String.class);
        verify(userDetailsRepository, times(1)).updateUserLastSeen(usernameArg.capture());
        assertEquals(USER.getUsername(), usernameArg.getValue());
    }

    @Test
    void should_ReturnListOfUserFavorite_When_MethodIsCalled() {
        //Arrange
        when(userDetailsRepository.findFirstByUsername(anyString()))
                .thenReturn(List.of(new UserFavoriteProjection() {
                    @Override
                    public Integer getId() {
                        return 1;
                    }

                    @Override
                    public Integer getUser_Id() {
                        return USER.getId();
                    }

                    @Override
                    public Integer getOffer_Id() {
                        return 1;
                    }
                }));

        //Act
        var result = userService.getUserFavoriteOffersIds(USER.getUsername());

        //Assert
        assertEquals(1, result.size());
    }

    @Test
    void should_ReturnEmptyListOfUserFavorite_When_MethodIsCalled() {
        //Arrange
        when(userDetailsRepository.findFirstByUsername(anyString()))
                .thenReturn(List.of());

        //Act
        var result = userService.getUserFavoriteOffersIds(USER.getUsername());

        //Assert
        assertEquals(0, result.size());
    }
}