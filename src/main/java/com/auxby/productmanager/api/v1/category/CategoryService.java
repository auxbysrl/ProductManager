package com.auxby.productmanager.api.v1.category;

import com.auxby.productmanager.api.v1.category.dto.CategoryInfoDto;
import com.auxby.productmanager.api.v1.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public List<CategoryInfoDto> getCategories(){
        return categoryRepository.findAll()
                .stream()
                .filter(category -> category.getParentName().isEmpty())
                .map(categoryMapper::mapToCategoryInfo)
                .collect(Collectors.toList());
    }
}
