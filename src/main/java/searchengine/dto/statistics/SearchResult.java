package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    private boolean result;
    private String error;
    private int count;
    private List<SearchData> data;

}
