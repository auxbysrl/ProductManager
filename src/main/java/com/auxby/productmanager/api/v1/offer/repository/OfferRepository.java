package com.auxby.productmanager.api.v1.offer.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer>, JpaSpecificationExecutor<Offer> {

    @Query("SELECT COUNT(o) FROM Offer o WHERE o.categoryId = :categoryId AND o.isAvailable = true")
    Long countActiveOffersByCategoryId(@Param("categoryId") Long categoryId);

    @EntityGraph(attributePaths = {"owner", "addresses", "contacts", "offerDetails", "files", "bids"})
    List<Offer> findAllByIdIn(List<Integer> ids);

    @EntityGraph(attributePaths = {"owner", "addresses", "contacts", "offerDetails", "files", "bids"})
    List<Offer> findByOwner_Username(String ownerUuid);

    @EntityGraph(attributePaths = {"owner", "addresses", "contacts", "offerDetails", "files", "bids"})
    Optional<Offer> findByIdAndOwner_Username(Integer integer, String ownerName);

    @Query("SELECT o FROM Offer o " +
            "JOIN FETCH o.owner " +
            "WHERE o.isAvailable = true AND o.isOnAuction = true AND o.id =:id")
    Optional<Offer> findByIdAndAvailableTrueAndOnAuctionTrue(Integer id);

    @Query("""
            SELECT o FROM Offer o JOIN o.owner ud
            WHERE o.publishDate >= :startOfDay AND
                  o.publishDate < :startOfNextDay AND
                  o.isOnAuction = false AND
                  o.autoExtend = true
            """)
    List<Offer> getOffersToAutoExtend(@Param("startOfDay") Date startOfDay,
                                      @Param("startOfNextDay") Date startOfNextDay);

    @Query("SELECT o FROM Offer o " +
            "JOIN FETCH o.owner " +
            "JOIN FETCH o.bids " +
            "WHERE o.expirationDate <= current_timestamp ")
    List<Offer> findAllExpiredOffers();

    @EntityGraph(attributePaths = {"owner", "addresses", "contacts", "offerDetails", "files", "bids"})
    Optional<Offer> findOfferById(Integer id);

    @Query("SELECT o FROM Offer o " +
            "WHERE o.isAvailable = true AND LOWER(o.name) LIKE %:title%")
    List<Offer> findAllOffersByTitleLike(String title);

    @Query("SELECT DISTINCT o FROM Offer o " +
            "LEFT JOIN FETCH o.bids as b " +
            "LEFT JOIN FETCH b.owner " +
            "WHERE o.isOnAuction = true AND o.isAvailable = true " +
            "AND o.auctionEndDate <= current_timestamp ")
    List<Offer> getAuctionsToClose();

    @Transactional
    @Modifying
    @Query("""
            UPDATE Offer o SET o.isAvailable = false
                        WHERE (o.isOnAuction = false AND o.publishDate < :thirtyDaysAgo)
                        OR (o.isOnAuction = true AND o.auctionEndDate < CURRENT_TIMESTAMP)
            """)
    void updateExpiredOffers(@Param("thirtyDaysAgo") Date thirtyDaysAgo);

    @Transactional
    @Modifying
    @Query("UPDATE Offer o SET o.isPromoted = false WHERE o.isPromoted = true AND o.promoteExpirationDate < CURRENT_TIMESTAMP")
    void updateExpiredPromotions();

    @Query(value = """
             SELECT DISTINCT result.*, 0 AS setAsFavoriteNumber
            FROM ((
                    SELECT of1.*
                    FROM offer of1 
                    WHERE of1.promote = 1
                      AND of1.is_available = 1
                      AND of1.promote_expiration_date > NOW()
                      AND of1.id NOT IN (-1)
                    ORDER BY RAND()
                    LIMIT 3
                ) UNION ALL (
                    SELECT of2.*
                    FROM offer of2
                    WHERE of2.promote = 1
                      AND of2.is_available = 1
                      AND of2.promote_expiration_date > NOW()
                      AND of2.id NOT IN (-1)
                    ORDER BY of2.views_number ASC, RAND()
                    LIMIT 2
                )
            ) AS result
            ORDER BY result.id;
              """, nativeQuery = true)
    List<Offer> findPromotedOffersExcluding(@Param("excludedIds") Set<Integer> excludedIds);

    @Query(value = """
            SELECT of3.* , 0 AS setAsFavoriteNumber
                   FROM offer of3
                   WHERE of3.promote = 0
                     AND of3.is_available = 1
                     AND of3.id NOT IN (:excludedIds)
                   ORDER BY of3.views_number ASC
                   LIMIT :limit
            """, nativeQuery = true)
    List<Offer> getRandomOffersExcluding(@Param("excludedIds") Set<Integer> excludedIds, int limit);

    @Query("""
            SELECT o.id as id, o.name as name, u.username as owner
            FROM Offer o
            LEFT JOIN o.owner u
            WHERE o.isAvailable = true
            AND (
                o.publishDate BETWEEN :publishStartDate AND :publishEndDate
                OR o.auctionEndDate BETWEEN :auctionStartDate AND :auctionEndDate
            )
            """)
    List<OfferSummaryProjection> findAvailableOffersWithinDateRanges(
            @Param("publishStartDate") Date publishStartDate,
            @Param("publishEndDate") Date publishEndDate,
            @Param("auctionStartDate") Date auctionStartDate,
            @Param("auctionEndDate") Date auctionEndDate
    );
}
