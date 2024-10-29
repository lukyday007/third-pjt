package com.singlebungle.backend.domain.ai.service;

import com.singlebungle.backend.global.model.BaseResponseBody;


public interface OpenaiService {

    BaseResponseBody requestImageAnalysis(String imageUrl, String requestText);
}
