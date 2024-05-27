package com.auxby.productmanager.api.v1.offer.specification;

import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearchCriteria;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import reactor.util.annotation.NonNull;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class OfferSpecification implements Specification<Offer> {

    private final OfferSearchCriteria searchCriteria;

    @Override
    public Predicate toPredicate(@NonNull Root<Offer> root,
                                 @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        if (!searchCriteria.getCategories().isEmpty()) {
            predicates.add(root.get("categoryId").in(searchCriteria.getCategories()));
        }
        if (!searchCriteria.getIsFixedPriced() && searchCriteria.getIsOnAuction()) {
            predicates.add(builder.isTrue(root.get("isOnAuction")));
        } else if (searchCriteria.getIsFixedPriced() && !searchCriteria.getIsOnAuction()) {
            predicates.add(builder.isFalse(root.get("isOnAuction")));
        }
        if (Objects.nonNull(searchCriteria.getTitle())) {
            String searchValue = getSearchValueForTitle(searchCriteria.getTitle());
            predicates.add(builder.like(builder.upper(root.get("name")), searchValue));
        }
        if (searchCriteria.getIsPromotedOnly()) {
            predicates.add(builder.isTrue(root.get("isPromoted")));
        }
        predicates.add(builder.isTrue(root.get("isAvailable")));

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    private String getSearchValueForTitle(String title) {
        String searchValue = title.replaceAll(" ", "%").toUpperCase();
        searchValue = "%" + searchValue + "%";

        return searchValue;
    }
}
