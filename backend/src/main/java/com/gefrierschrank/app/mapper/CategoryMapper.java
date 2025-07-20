package com.gefrierschrank.app.mapper;

import com.gefrierschrank.app.dto.CategoryDto;
import com.gefrierschrank.app.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setIcon(category.getIcon());
        dto.setDefaultUnit(category.getDefaultUnit());
        dto.setUnitStep(category.getUnitStep());
        dto.setMinValue(category.getMinValue());
        dto.setMaxValue(category.getMaxValue());

        return dto;
    }

    public Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setDefaultUnit(dto.getDefaultUnit());
        category.setUnitStep(dto.getUnitStep());
        category.setMinValue(dto.getMinValue());
        category.setMaxValue(dto.getMaxValue());

        return category;
    }

    public void updateEntityFromDto(CategoryDto dto, Category category) {
        if (dto == null || category == null) {
            return;
        }

        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setDefaultUnit(dto.getDefaultUnit());
        category.setUnitStep(dto.getUnitStep());
        category.setMinValue(dto.getMinValue());
        category.setMaxValue(dto.getMaxValue());
    }
}