package com.auxby.productmanager.api.v1.category.dto;

import java.util.List;

public record OptionDto(String name, List<String> childOptions, String parentOption, List<LocalizationDto> label) {

}
