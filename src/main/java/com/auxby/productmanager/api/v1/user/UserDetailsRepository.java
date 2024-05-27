package com.auxby.productmanager.api.v1.user;

import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {
    Optional<UserDetails> findUserDetailsByUsername(String username);

    @Modifying
    @Query("update UserDetails u set u.availableCoins = :availableCoins " +
            "where u.username = :username")
    void updateUserCoinsNumber(String username, Integer availableCoins);

    @Modifying
    @Query(value = "update user_details set last_seen = current_timestamp " +
            "where username = :username", nativeQuery = true)
    void updateUserLastSeen(String username);

    @Query(value = "SELECT * FROM favorite " +
            "WHERE user_id = (SELECT ud.id FROM user_details ud " +
            "WHERE ud.username =:username)", nativeQuery = true)
    List<UserFavoriteProjection> findFirstByUsername(String username);
}
