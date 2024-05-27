package com.auxby.productmanager.api.v1.favorite;

import com.auxby.productmanager.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    Optional<Favorite> findFirstByOfferIdAndAndUserId(Integer offerId, Integer userId);

    void deleteAllByOfferId(int offerId);
}
