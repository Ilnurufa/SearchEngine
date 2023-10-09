package searchengine.dto.statistics;

import lombok.Data;
import searchengine.model.Portal;

@Data
public class SearchData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

}
