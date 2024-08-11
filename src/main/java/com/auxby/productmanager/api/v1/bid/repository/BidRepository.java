package com.auxby.productmanager.api.v1.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {

    @Query("SELECT b FROM Bid b " +
            "left join fetch b.offer o " +
            "left join fetch o.addresses " +
            "left join fetch o.contacts " +
            "left join fetch o.bids " +
            "left join fetch o.files " +
            "left join fetch o.owner " +
            "where b.owner.id =:userId")
    List<Bid> getUserBids(Integer userId);
}
