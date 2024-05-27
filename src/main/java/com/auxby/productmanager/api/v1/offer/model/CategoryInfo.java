package com.auxby.productmanager.api.v1.offer.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInfo {
    Long id;
    String icon;
    List<Localization> label;
    long noOffers;
}
