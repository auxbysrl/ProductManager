package com.auxby.productmanager.api.v1.category.dto;

import java.util.List;

public record CategoryDetailDto(
        Integer guiOrder,
        String parent,
        String name,
        String type,
        List<LocalizationDto> label,
        String placeholder,
        Boolean required,
        List<OptionDto> options,
        ConstraintsDto constraints
) {

}
