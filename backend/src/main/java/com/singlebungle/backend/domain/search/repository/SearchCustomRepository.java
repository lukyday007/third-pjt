package com.singlebungle.backend.domain.search.repository;

import com.singlebungle.backend.domain.search.entity.SearchDocument;

import java.util.List;

public interface SearchCustomRepository {

    List<SearchDocument> searchByWildcardAndBoost(String tag);

}
