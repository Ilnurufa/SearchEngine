package searchengine.services.Indexing;

import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;

public interface IndexingInt {
    IndexingResponse startIndexing();

    StopIndexingResponse stopIndexing();

    IndexPageResponse pageIndex(String pageUrl);
}
