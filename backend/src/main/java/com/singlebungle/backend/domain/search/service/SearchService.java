package com.singlebungle.backend.domain.search.service;

import java.util.List;

public interface SearchService {

    void saveTags(List<String> tags, String imageUrl);

    List<String> getKeywordsByTag(String keyword);

    void incrementSearchCount(String keyword);

}
