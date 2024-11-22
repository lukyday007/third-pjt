package com.singlebungle.backend.domain.search.service;

import java.util.List;

public interface SearchService {

    void saveTagsByKeywords(List<String> tags, String imageUrl);

    List<String> getKeywordsByTag(String keyword);


}
