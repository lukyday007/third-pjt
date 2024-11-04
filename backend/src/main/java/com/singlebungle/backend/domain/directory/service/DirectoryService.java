package com.singlebungle.backend.domain.directory.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.user.entity.User;

import java.util.List;

public interface DirectoryService {

//    void saveDirectory(String name);

    List<Directory> createDirectory(String directoryName, String token);

    List<Directory> updateDirectoryName(Long directoryId, String directoryName, String token);

    List<Directory> getUserDirectories(String token);

    List<Directory> updateDirectorySequence(List<Long> directorySequence, String token);

    List<Directory> deleteDirectory(Long directoryId, String token);

    void createDefaultDirectories(User user);
}
