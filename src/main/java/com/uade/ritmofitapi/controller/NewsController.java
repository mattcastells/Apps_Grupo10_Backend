package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.response.NewsResponse;
import com.uade.ritmofitapi.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<List<NewsResponse>> getAllNews() {
        List<NewsResponse> news = newsService.getAllNews();
        return ResponseEntity.ok(news);
    }
}
