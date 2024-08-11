package com.auxby.productmanager.api.v1.user;

import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserDetailsRepository userDetailsRepository;

    public Optional<UserDetails> getUser() {
        String username = SecurityContextUtil.getUsername();
        return userDetailsRepository.findUserDetailsByUsername(username);
    }

    public void updateUserCoinsNumber(String username, Integer numberOfCoins) {
        userDetailsRepository.updateUserCoinsNumber(username, numberOfCoins);
    }

    @Transactional
    public void updateUserLastSeen(String username) {
        userDetailsRepository.updateUserLastSeen(username);
    }

    public List<Integer> getUserFavoriteOffersIds(String username) {
        var userFavoriteOffers = userDetailsRepository.findFirstByUsername(username);
        if (userFavoriteOffers.isEmpty()) {
            return new ArrayList<>();
        }
        return userFavoriteOffers.stream()
                .map(UserFavoriteProjection::getOffer_Id)
                .toList();
    }
}
