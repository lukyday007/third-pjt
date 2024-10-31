package com.singlebungle.backend.domain.directory.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final DirectoryRepository directoryRepository;

    @Override
    @Transactional
    public void saveDirectory(String name) {

        if (directoryRepository.existsByName(name)) {
            throw new EntityIsFoundException("해당 디렉토리 데이터가 이미 존재합니다.");
        }

        Directory directory = Directory.convertToEntity(name);

        directoryRepository.save(directory);
    }
}
