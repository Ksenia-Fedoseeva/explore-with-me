package ru.practicum.ewm.service.pub.compilation;

import ru.practicum.ewm.dto.compilation.CompilationDto;

import java.util.List;

public interface PublicCompilationService {
    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long compId);
}

