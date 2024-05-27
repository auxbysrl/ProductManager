package com.auxby.productmanager.api.v1.offer.specification.criteria;

import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.DetailsFilter;
import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.PriceFilter;
import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.SortDetails;
import com.auxby.productmanager.utils.enums.ConditionType;
import lombok.Data;

import java.util.List;

@Data
public class AdvancedOfferSearchCriteria extends OfferSearchCriteria {
    private List<DetailsFilter> detailsFilters;
    private ConditionType conditionType;
    private PriceFilter priceFilter;
    private SortDetails sortDetails;
    private String location;
}
