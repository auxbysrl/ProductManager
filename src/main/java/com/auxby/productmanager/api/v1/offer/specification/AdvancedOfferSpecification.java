package com.auxby.productmanager.api.v1.offer.specification;

import com.amazonaws.util.StringUtils;
import com.auxby.productmanager.api.v1.commun.entity.Address;
import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfiguration;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearch;
import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.PriceFilter;
import com.auxby.productmanager.utils.enums.OfferType;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class AdvancedOfferSpecification implements Specification<Offer> {

    private static final String OFFER_PRICE_KEY = "price";

    private final OfferSearch criteria;
    private final SystemConfiguration configuredCurrency;

    @Override
    public Predicate toPredicate(@NonNull Root<Offer> root,
                                 @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Main predicate - based on Offer
        if (criteria.categories() != null && !criteria.categories().isEmpty()) {
            predicates.add(root.get("categoryId").in(criteria.categories()));
        }

        if (Objects.nonNull(criteria.offerType())) {
            if (criteria.offerType() == OfferType.AUCTION) {
                predicates.add(cb.isTrue(root.get("isOnAuction")));
            } else {
                predicates.add(cb.isFalse(root.get("isOnAuction")));
            }
        }

        if (Objects.nonNull(criteria.title())) {
            String searchValue = getSearchValueForTitle(criteria.title());
            predicates.add(cb.like(cb.upper(root.get("name")), searchValue));
        }
        if (Objects.nonNull(criteria.conditionType())) {
            predicates.add(cb.equal(root.get("condition"), criteria.conditionType()));
        }
        if (StringUtils.hasValue(criteria.location())) {
            Join<Offer, Address> offerAddressJoin = root.join("addresses");
            predicates.add(cb.equal(offerAddressJoin.get("city"), criteria.location()));
        }

        predicates.add(cb.isTrue(root.get("isAvailable")));

        Predicate mainPredicate = cb.and(predicates.toArray(new Predicate[0]));

        //Price predicate
        Predicate pricePredicate = getPredicateForPrice(criteria.priceFilter(), root, cb);

        //Combine predicates and set sorting
        Predicate finalPredicate = cb.and(mainPredicate, pricePredicate);
        query.groupBy(root.get("id"));

        if (Objects.nonNull(criteria.sortDetails()) && criteria.sortDetails().orderType().equalsIgnoreCase("ASC")) {
            query.orderBy(cb.asc(root.get(getSortingKey())));
        } else {
            query.orderBy(cb.desc(root.get(getSortingKey())));
        }

        return finalPredicate;
    }

    private String getSearchValueForTitle(String title) {
        String searchValue = title.replaceAll(" ", "%").toUpperCase();
        searchValue = "%" + searchValue + "%";

        return searchValue;
    }

    private Predicate getPredicateForPrice(PriceFilter priceFilter,
                                           @NonNull Root<Offer> root,
                                           @NonNull CriteriaBuilder cb) {
        if (Objects.isNull(priceFilter)
                || Objects.isNull(priceFilter.currencyType())) {
            return cb.and();
        }
        var highestPrice = priceFilter.currencyType().toRonConversion(priceFilter.highestPrice(), configuredCurrency);
        var lowestPrice = priceFilter.currencyType().toRonConversion(priceFilter.lowestPrice(), configuredCurrency);

        if (Objects.nonNull(priceFilter.lowestPrice()) && Objects.nonNull(priceFilter.highestPrice())) {
            return cb.between(root.get(OFFER_PRICE_KEY), lowestPrice, highestPrice);
        }
        if (Objects.isNull(priceFilter.highestPrice()) && Objects.nonNull(priceFilter.lowestPrice())) {
            return cb.greaterThanOrEqualTo(root.get(OFFER_PRICE_KEY), lowestPrice);
        }
        if (Objects.nonNull(priceFilter.highestPrice())) {
            return cb.lessThanOrEqualTo(root.get(OFFER_PRICE_KEY), highestPrice);
        }

        return cb.and();
    }

    private String getSortingKey() {
        if (Objects.isNull(criteria.sortDetails())) {
            return "publishDate";
        }
        return Objects.isNull(criteria.sortDetails().key()) ? "publishDate" : criteria.sortDetails().key();
    }

}
