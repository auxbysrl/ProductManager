package com.auxby.productmanager.api.v1.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addRemoveOfferFromFavorite(Integer userId, Integer offerId) {
        Optional<Favorite> favorite = favoriteRepository.findFirstByOfferIdAndAndUserId(offerId, userId);
        if (favorite.isPresent()) {
            favoriteRepository.deleteById(favorite.get().getId());
        } else {
            addToFavorite(userId, offerId);
        }
    }

    public void removeOfferFromFavorites(Integer id) {
        favoriteRepository.deleteAllByOfferId(id);
    }

    private void addToFavorite(Integer userId, Integer offerId) {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setOfferId(offerId);

        favoriteRepository.save(favorite);
    }
}
