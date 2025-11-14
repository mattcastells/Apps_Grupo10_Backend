package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.NewsResponse;
import com.uade.ritmofitapi.model.News;
import com.uade.ritmofitapi.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<NewsResponse> getAllNews() {
        List<News> newsList = newsRepository.findAllByOrderByPublishedDateDesc();
        return newsList.stream()
                .map(this::mapToNewsResponse)
                .collect(Collectors.toList());
    }

    private NewsResponse mapToNewsResponse(News news) {
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.getImageUrl(),
                news.getPublishedDate().format(DATE_FORMATTER)
        );
    }
}
