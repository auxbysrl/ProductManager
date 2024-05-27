package com.auxby.productmanager.api.v1.category.dto;

import java.util.List;

public record CategoryDetailsInfoDto(
        Long id,
        String icon,
        String name,
        List<LocalizationDto> label,
        String parentName,
        Integer addOfferCost,
        Integer placeBidCost,
        List<CategoryDetailDto> categoryDetails,
        List<CategoryDetailsInfoDto> subcategories
) {

}