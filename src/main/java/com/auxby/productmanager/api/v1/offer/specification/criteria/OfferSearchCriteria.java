package com.auxby.productmanager.api.v1.offer.specification.criteria;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferSearchCriteria {
    @Builder.Default
    @Parameter(in = ParameterIn.QUERY, description = "List of categories ids.")
    private List<Integer> categories = new ArrayList<>();
    @Builder.Default
    @Parameter(in = ParameterIn.QUERY, description = "Is offer on Auction.")
    private Boolean isOnAuction = false;
    @Builder.Default
    @Parameter(in = ParameterIn.QUERY, description = "Is an fixed priced offer.")
    private Boolean isFixedPriced = false;
    @Builder.Default
    @Parameter(in = ParameterIn.QUERY, description = "Is an promoted offers only.")
    private Boolean isPromotedOnly = false;
    @Parameter(in = ParameterIn.QUERY, description = "Title.")
    private String title;
    @Builder.Default
    @Parameter(in = ParameterIn.QUERY, description = "User name.")
    private String userName = "";
}
