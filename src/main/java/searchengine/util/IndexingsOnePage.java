package searchengine.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.util.LemmaIndex;

import java.io.IOException;

public class IndexingsOnePage {

    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private String pageAddressIndexings;
    private Portal updatePortal;

    public IndexingsOnePage(PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, String pageAddressIndexings
            , Portal updatePortal) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.pageAddressIndexings = pageAddressIndexings;
        this.updatePortal = updatePortal;
        DeleteOnePage();
    }

    public void DeleteOnePage() {
        try {
            String path = pageAddressIndexings.substring(pageAddressIndexings.indexOf("/", 8), pageAddressIndexings.length());
            int code = 0;
            Page delPage = pageRepository.findByPath(path);
            if (delPage != null) {
                lemmaRepository.updateFrequency(delPage.getId());
                pageRepository.deletePage(delPage.getId());
            }
            Page addPage = new Page();
            Connection connectionUrl = Jsoup.connect(pageAddressIndexings);
            code = connectionUrl.execute().statusCode();
            Document document = connectionUrl
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .ignoreHttpErrors(true)
                    .referrer("http://www.google.com")
                    .maxBodySize(0)
                    .get();
            String content = document.toString().replace("'", "\\'");
            addPage.setCode(code);
            addPage.setPath(path);
            addPage.setContent(content);
            addPage.setSite(updatePortal);
            pageRepository.save(addPage);
            int temp4 = 1;
            new LemmaIndex(updatePortal, addPage, document, lemmaRepository, indexRepository, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addOnePage(int siteId) {


    }

}
