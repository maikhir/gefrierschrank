package com.gefrierschrank.app.mapper;

import com.gefrierschrank.app.dto.CreateItemRequest;
import com.gefrierschrank.app.dto.ItemDto;
import com.gefrierschrank.app.dto.UpdateItemRequest;
import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.entity.Item;
import com.gefrierschrank.app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }

        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setCategoryId(item.getCategory().getId());
        dto.setCategoryName(item.getCategory().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnit(item.getUnit());
        dto.setExpiryDate(item.getExpiryDate());
        dto.setExpiryType(item.getExpiryType());
        dto.setDescription(item.getDescription());
        dto.setPhotoPath(item.getPhotoPath());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        return dto;
    }

    public Item toEntity(CreateItemRequest request, Category category, User user) {
        if (request == null) {
            return null;
        }

        Item item = new Item();
        item.setName(request.getName());
        item.setCategory(category);
        item.setUser(user);
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setExpiryDate(request.getExpiryDate());
        item.setExpiryType(request.getExpiryType());
        item.setDescription(request.getDescription());
        item.setPhotoPath(request.getPhotoPath());

        return item;
    }

    public void updateEntityFromDto(UpdateItemRequest request, Item item, Category category) {
        if (request == null || item == null) {
            return;
        }

        item.setName(request.getName());
        item.setCategory(category);
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setExpiryDate(request.getExpiryDate());
        item.setExpiryType(request.getExpiryType());
        item.setDescription(request.getDescription());
        item.setPhotoPath(request.getPhotoPath());
    }
}