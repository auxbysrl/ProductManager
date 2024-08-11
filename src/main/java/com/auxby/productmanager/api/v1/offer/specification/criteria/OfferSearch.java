package com.auxby.productmanager.api.v1.offer.specification.criteria;

import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.PriceFilter;
import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.SortDetails;
import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.OfferType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.util.List;

public record OfferSearch(
        @Parameter(in = ParameterIn.QUERY, description = "List of categories ids.")
        List<Integer> categories,
        @Parameter(in = ParameterIn.QUERY, description = "Offer type : Auction or Fix price")
        OfferType offerType,
        @Parameter(in = ParameterIn.QUERY, description = "Title.")
        String title,
        @Parameter(in = ParameterIn.QUERY, description = "Condition : New or Used")
        ConditionType conditionType,
        @Parameter(in = ParameterIn.QUERY, description = "Price filter.")
        PriceFilter priceFilter,
        @Parameter(in = ParameterIn.QUERY, description = "Sort details.")
        SortDetails sortDetails,
        @Parameter(in = ParameterIn.QUERY, description = "Location.")
        String location
) {
}
