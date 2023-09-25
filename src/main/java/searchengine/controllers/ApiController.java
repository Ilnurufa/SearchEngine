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
        if (onePageIndexing.getUrl().equals("")) {
            throw new BadRequestException("Не указана страница для индексации");
        }
        indexPageResponse = indexingInt.pageIndex(onePageIndexing.getUrl());
        if (indexPageResponse.isResult()) {
            return ResponseEntity.ok(indexPageResponse);
        } else {
            throw new ResourceNotFoundException("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPage(SearchRequest searchRequest) {
        SearchResult searchResult;
        if (searchRequest.getQuery().equals("")) {
            throw new BadRequestException("Задан пустой поисковый запрос!");
        }
        searchResult = searchService.search(searchRequest.getQuery(), searchRequest.getSite(), searchRequest.getLimit(), searchRequest.getOffset());
        if (searchResult.isResult()) {
            return ResponseEntity.ok(searchResult);
        } else {
            throw new ResourceNotFoundException("Поисковый запрос не дал результатоввввв!");

        }

    }

}
