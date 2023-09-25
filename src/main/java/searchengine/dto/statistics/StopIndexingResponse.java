package searchengine.dto.statistics;

import lombok.Data;

@Data
public class StopIndexingResponse {

    private boolean result;
    private String error;
}
