package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.*;
import searchengine.exception.BadRequestException;
import searchengine.exception.ResourceNotFoundException;
import searchengine.services.Indexing.IndexingInt;
import searchengine.services.statistics.StatisticsService;
import searchengine.services.search.SearchService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingInt indexingInt;
    private final SearchService searchService;


    public ApiController(StatisticsService statisticsService, IndexingInt indexingInt, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingInt = indexingInt;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(indexingInt.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<StopIndexingResponse> stopIndexing() {
        return ResponseEntity.ok(indexingInt.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexPageResponse> indexOnePage(OnePageIndexing onePageIndexing) {
        IndexPageResponse indexPageResponse;
        indexPageResponse = indexingInt.pageIndex(onePageIndexing.getUrl());
        return ResponseEntity.ok(indexPageResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPage(SearchRequest searchRequest) {
        SearchResult searchResult;
        searchResult = searchService.search(searchRequest.getQuery(), searchRequest.getSite(), searchRequest.getLimit(), searchRequest.getOffset());
            return ResponseEntity.ok(searchResult);
    }

}
