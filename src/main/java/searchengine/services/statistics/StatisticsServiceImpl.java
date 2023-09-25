package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.*;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.statistics.StatisticsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        ArrayList<Portal> portals = (ArrayList<Portal>) siteRepository.findAll();

        total.setSites(portals.size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for(Portal portal : portals){
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(portal.getName());
            item.setUrl(portal.getUrl());
            int pages = pageRepository.countAllBySiteId(portal.getId());
            int lemmas = lemmaRepository.countAllBySiteId(portal.getId());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(portal.getAnEnum().name());
            item.setStatusTime(portal.getStatusTime().getTime());
            item.setError(portal.getLastError());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
