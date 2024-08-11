package com.auxby.productmanager.api.v1.favorite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;
    @Mock
    private FavoriteRepository favoriteRepository;

    @Test
    void should_AddOfferToFavorite_WhenMethodIsCalled() {
        //Arrange
        when(favoriteRepository.findFirstByOfferIdAndAndUserId(1, 1)).thenReturn(Optional.empty());

        //Act
        favoriteService.addRemoveOfferFromFavorite(1, 1);

        //Assert
        ArgumentCaptor<Favorite> saveArg = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository, times(1)).save(saveArg.capture());
        assertEquals(1, saveArg.getValue().getOfferId());
        assertEquals(1, saveArg.getValue().getUserId());
    }

    @Test
    void should_RemoveOfferFromFavorite_WhenMethodIsCalled() {
        //Arrange
        Favorite favorite = new Favorite();
        favorite.setId(1);
        favorite.setOfferId(1);
        favorite.setOfferId(1);
        when(favoriteRepository.findFirstByOfferIdAndAndUserId(1, 1)).thenReturn(Optional.of(favorite));

        //Act
        favoriteService.addRemoveOfferFromFavorite(1, 1);

        //Assert
        ArgumentCaptor<Integer> deleteArg = ArgumentCaptor.forClass(Integer.class);
        verify(favoriteRepository, times(1)).deleteById(deleteArg.capture());
        assertEquals(1, deleteArg.getValue());
    }

    @Test
    void should_RemoveAllOfferFavoriteSaves_WhenMethodIsCalled() {
        //Act
        favoriteService.removeOfferFromFavorites(1);

        //Assert
        verify(favoriteRepository, times(1)).deleteAllByOfferId(1);
    }
}