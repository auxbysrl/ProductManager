package com.auxby.productmanager.api.v1.category.dto;

public record ConstraintsDto(
        Integer maxLength,
        Integer maxLines,
        String inputType
) {

}
