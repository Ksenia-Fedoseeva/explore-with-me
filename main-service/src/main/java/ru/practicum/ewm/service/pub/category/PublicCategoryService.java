package ru.practicum.ewm.service.pub.category;

import ru.practicum.ewm.dto.category.CategoryDto;

import java.util.List;

public interface PublicCategoryService {
    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);
}
