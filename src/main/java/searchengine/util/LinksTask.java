package searchengine.util;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;


public class LinksTask extends RecursiveAction {

    private ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<>();
    private static final CopyOnWriteArraySet<String> PASSED = new CopyOnWriteArraySet<>();
    private boolean flagMainLinkOrPage = true;
    private String siteUrl;
    private String newSiteAddress;
    private static PageRepository pageRepository;
    private static SiteRepository siteRepository;
    private static LemmaRepository lemmaRepository;
    private static IndexRepository indexRepository;
    private static boolean stopIndexFlag = true;
    public static List<String> list = Arrays.asList(".jpg", ".jpeg", ".xml", ".pdf", ".doc", ".docx", ".xlsx", ".rar"
            , ".7zip", ".ppt", ".nc", ".zip", ".png");


    public LinksTask() {
    }

    public LinksTask(String siteUrl, PageRepository pageRepository, SiteRepository siteRepository,
                     LemmaRepository lemmaRepository, IndexRepository indexRepository, boolean flagMainLinkOrPage, boolean stopIndexFlag) {
        this.siteUrl = siteUrl;
        LinksTask.pageRepository = pageRepository;
        LinksTask.siteRepository = siteRepository;
        LinksTask.stopIndexFlag = stopIndexFlag;
        LinksTask.lemmaRepository = lemmaRepository;
        LinksTask.indexRepository = indexRepository;
    }

    public LinksTask(String Url) {
        this.siteUrl = Url;
    }

    @Override
    protected void compute() {
        if (stopIndexFlag) {
            getLinks(siteUrl);
            List<LinksTask> taskList = new ArrayList<>();
            while (!(deque.peek() == null)) {
                LinksTask task = new LinksTask(deque.pollFirst());
                task.fork();
                taskList.add(task);
            }
            for (LinksTask task : taskList) {
                task.join();
            }
        }
    }

    public void getLinks(String url) {
        Connection connectionUrl = Jsoup.connect(url);
        Connection.Response response;
        String path;
        int code;
        String content;
        try {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Document document = connectionUrl
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .ignoreHttpErrors(true)
                    .referrer("http://www.google.com")
                    .maxBodySize(0)
                    .get();
            response = connectionUrl.execute();
            Elements allLinks = document.select("a");
            if(siteUrl.contains(".ru")) {
                newSiteAddress = siteUrl.substring(0, siteUrl.indexOf(".ru") + 4);
            }
            else {
                newSiteAddress = siteUrl.substring(0, siteUrl.indexOf(".com") + 5);
            }
            for (Element element : allLinks) {
                String href = element.absUrl("href");
                if (stopIndexFlag && (href.startsWith(newSiteAddress)) && (!href.contains("#")) && (!href.equals(newSiteAddress)) && (!PASSED.contains(href))
                        && !list.contains(href.substring(href.lastIndexOf('.')))) {
                    PASSED.add(href);
                    deque.add(href);
                }
            }
            if (newSiteAddress.equals(url)) {
                path = "/";
            } else {
                if (newSiteAddress.endsWith("/")) {
                    path = url.substring(newSiteAddress.length() - 1);
                } else {
                    path = url.substring(newSiteAddress.length());
                }
            }
            content = document.toString().replace("'", "\\'");
            code = response.statusCode();
            if (!newSiteAddress.equals(url) || flagMainLinkOrPage == true) {
                recordRepository(path, content, code, document);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void recordRepository(String path, String content, int code, Document document) {
        Portal portal = siteRepository.findByUrl(newSiteAddress);
        Page page = new Page();
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        page.setSite(portal);
        pageRepository.save(page);
        Optional<Portal> optionalPage = siteRepository.findById(portal.getId());
        optionalPage.ifPresent(portal1 -> {
            portal.setStatusTime(new Date());
            siteRepository.save(portal);
        });
        new LemmaIndex(portal, page, document, lemmaRepository, indexRepository, false);
    }

    public static void setStopIndexFlag(boolean stopIndexFlag) {
        LinksTask.stopIndexFlag = stopIndexFlag;
    }

    public static void clearPASSED() {
        PASSED.clear();
    }

}



