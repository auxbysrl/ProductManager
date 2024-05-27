package com.auxby.productmanager.api.v1.category.dto;

import java.util.List;

public record CategoryInfoDto(Long id, String icon, List<LocalizationDto> label) {

}