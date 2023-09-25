package searchengine.services.search;

import searchengine.dto.statistics.SearchResult;

public interface SearchService {

    SearchResult search(String searchRequest, String site, int limit, int offset);
}
