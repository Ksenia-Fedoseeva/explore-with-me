package ru.practicum.ewm.service.admin.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Имя категории должно быть уникальным: " + newCategoryDto.getName());
        }

        Category category = categoryRepository.save(CategoryMapper.toEntity(newCategoryDto));
        return CategoryMapper.toDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id" + catId + " не найдена"));

        if (!category.getName().equals(categoryDto.getName())
                && categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Имя категории должно быть уникальным: " + categoryDto.getName());
        }

        category.setName(categoryDto.getName());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id" + catId + " не найдена"));

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Категория не пустая");
        }

        categoryRepository.delete(category);
    }
}
