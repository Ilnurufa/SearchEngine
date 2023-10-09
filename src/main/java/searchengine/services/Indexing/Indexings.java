package searchengine.services.Indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;
import searchengine.exception.BadRequestException;
import searchengine.exception.ResourceNotFoundException;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.IndexingsOnePage;
import searchengine.util.LinksTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static searchengine.model.Status.INDEXING;

@Service
@RequiredArgsConstructor
public class Indexings implements IndexingInt {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;

    private final SitesList sites;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Future future;
    private ForkJoinPool forkJoinPool;

    public IndexingResponse startIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if (future != null) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена");
        } else {
            future = executorService.submit(this::siteParser);
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }

    public void siteParser() {
        List<Site> sitesList = sites.getSites();
        forkJoinPool = new ForkJoinPool();
        for (Site site : sitesList) {
            Runnable runnable =
                    () -> {
                        siteRepository.deleteByName(site.getName());
                        siteRepository.resetIndex(); //если таблица не пуста то auto increment не сбрасывается
                        Portal portal = new Portal();
                        portal.setName(site.getName());
                        portal.setUrl(site.getUrl());
                        portal.setAnEnum(INDEXING);
                        portal.setStatusTime(new Date());
                        siteRepository.save(portal);
                        int siteId = portal.getId();
                        forkJoinPool.invoke(new LinksTask(portal.getUrl(), pageRepository, siteRepository, lemmaRepository, indexRepository, true, true));
                        siteRepository.updateStatus(siteId);
                    };
            Thread threadForSite = new Thread(runnable);
            threadForSite.setName(site.getName());
            threadForSite.start();
        }
    }

    public StopIndexingResponse stopIndexing() {
        StopIndexingResponse stopIndexingResponse = new StopIndexingResponse();
        try {
            if (future.isDone()) {
                LinksTask.setStopIndexFlag(false);
                LinksTask.clearPASSED();
                forkJoinPool.shutdownNow();
                executorService.shutdownNow();
                executorService = Executors.newFixedThreadPool(1);
                future.cancel(true);
                future = null;
                stopIndexingResponse.setResult(true);
                ArrayList<Integer> tempId = siteRepository.findStatusIndexing();
                for (int siteIdTemp : tempId) {
                    siteRepository.updateStatusStopIndexing(siteIdTemp);
                }
            } else {
                stopIndexingResponse.setResult(false);
                stopIndexingResponse.setError("Индексация не запущена");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stopIndexingResponse;

    }

    public IndexPageResponse pageIndex(String pageUrl) {
        int count = 0;
        if (pageUrl.equals("")) {
            throw new BadRequestException("Не указана страница для индексации");
        }
        IndexPageResponse indexPageResponse = new IndexPageResponse();
        int code = 0;
        try {
            Connection connectionUrl = Jsoup.connect(pageUrl).ignoreHttpErrors(true);
            code = connectionUrl.execute().statusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (code != 200) {
            indexPageResponse.setResult(false);
            throw new ResourceNotFoundException("Данной страницы не существует");
        }
        List<Site> sitesList = sites.getSites();
        String siteAddress2 = pageUrl.substring(0, pageUrl.indexOf("/", 8))
                .replaceAll("www.", "");
        String addressWithoutProtocol = siteAddress2.substring(siteAddress2.indexOf("//") + 2);
        for (Site site : sitesList) {
            if (pageUrl.contains(site.getUrl())) {
                count++;
            }
            String siteAddressWithoutProtocol = site.getUrl().substring(0, site.getUrl().indexOf("/", 8))
                    .replaceAll("www.", "");
            String siteAddress = siteAddressWithoutProtocol.substring(siteAddressWithoutProtocol.indexOf("//") + 2);
            if (siteAddress.equals(addressWithoutProtocol)) {
                Portal updatePortal = siteRepository.findByUrl(site.getUrl());
                StartParsOnePage(pageUrl, updatePortal);
                indexPageResponse.setResult(true);
                break;
            }
        }
        if (count == 0) {
            throw new ResourceNotFoundException("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n");
        }
        return indexPageResponse;
    }

    public void StartParsOnePage(String pageUrl, Portal portal) {
        new IndexingsOnePage(pageRepository, lemmaRepository, indexRepository, pageUrl, portal);
    }
}
