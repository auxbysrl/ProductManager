package com.auxby.productmanager.api.v1.category;

import com.auxby.productmanager.api.v1.category.dto.CategoryInfoDto;
import com.auxby.productmanager.api.v1.category.repository.Category;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface CategoryMapper {

    CategoryInfoDto mapToCategoryInfo(Category category);

}
