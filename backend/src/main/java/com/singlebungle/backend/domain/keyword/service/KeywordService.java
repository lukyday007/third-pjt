package com.singlebungle.backend.domain.keyword.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface KeywordService {

    void save(List<String> keywords);
}
