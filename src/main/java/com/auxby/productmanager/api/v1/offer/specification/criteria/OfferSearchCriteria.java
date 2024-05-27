package com.auxby.productmanager.api.v1.offer.specification.criteria;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfferSearchCriteria {
    @Parameter(in = ParameterIn.QUERY, description = "List of categories ids.")
    private List<Integer> categories = new ArrayList<>();
    @Parameter(in = ParameterIn.QUERY, description = "Is offer on Auction.")
    private Boolean isOnAuction = false;
    @Parameter(in = ParameterIn.QUERY, description = "Is an fixed priced offer.")
    private Boolean isFixedPriced = false;
    @Parameter(in = ParameterIn.QUERY, description = "Is an promoted offers only.")
    private Boolean isPromotedOnly = false;
    @Parameter(in = ParameterIn.QUERY, description = "Title.")
    private String title;
}
