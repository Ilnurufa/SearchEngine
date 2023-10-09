package searchengine.services.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.SearchData;
import searchengine.dto.statistics.SearchResult;
import searchengine.exception.BadRequestException;
import searchengine.exception.ResourceNotFoundException;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.LemmaIndex;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    LemmaIndex lemmaIndex = new LemmaIndex();
    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    public static HashMap<Integer, List<Integer>> pagesAndHerLemmas = new HashMap<>();
    public static HashSet<String> lemmasFromSearchRequest = new HashSet<>();

    public SearchResult search(String searchRequest, String site, int limit, int offset) {
        List<Object[]> finalFoundPages = new ArrayList<>();
        SearchResult searchResult = new SearchResult();
        if (!searchRequest.equals("")) {
            List<String> lemmaForSearch = LemmaIndex.getLemma(searchRequest);
            lemmasFromSearchRequest = new HashSet<>(lemmaForSearch);
            HashSet<Integer> listSiteIdWhereLemmaFound = lemmaRepository.siteIdWhereLemmaFound(lemmasFromSearchRequest);
            if (listSiteIdWhereLemmaFound.size() == 0) {
                throw new ResourceNotFoundException("Поисковый запрос не дал результатов!");
            }
            if (site == null) {
                for (int siteId : listSiteIdWhereLemmaFound) {
                    finalFoundPages.addAll(finalFoundPage(siteId));
                }
            } else {
                finalFoundPages.addAll(finalFoundPage(siteRepository.findByUrl(site).getId()));
            }
            if (finalFoundPages.isEmpty()) {
                searchResult.setResult(false);
                return searchResult;
            }
            List<SearchData> listData = getSearchData(finalFoundPages);
            int countForSubstring = Math.min((offset + limit), listData.size());
            if (listData.size() > 10) {
                listData = listData.subList(offset, countForSubstring);
            }
            searchResult.setData(listData);
            searchResult.setResult(true);
            searchResult.setCount(finalFoundPages.size());
        } else {
            throw new BadRequestException("Задан пустой поисковый запрос!");
        }
        return searchResult;
    }

    public List<Object[]> finalFoundPage(int siteId) {
        List<Object[]> foundPages = new ArrayList<>();
        List<Integer> lemmaFromRequestSortAsk = lemmaRepository.lemmaByAsc(lemmasFromSearchRequest, siteId);
        if (lemmaFromRequestSortAsk.isEmpty()) {
            return foundPages;
        }
        ArrayList<Integer> idPagesWithRareLemma = indexRepository.foundPages(lemmaFromRequestSortAsk.get(0));
        for (int pageId : idPagesWithRareLemma) {
            List<Integer> listIdLemmaInPage = indexRepository.findLemmasInPage(pageId);
            pagesAndHerLemmas.put(pageId, listIdLemmaInPage);
        }
        for (int lemmaId : lemmaFromRequestSortAsk) {
            pagesAndHerLemmas.entrySet().removeIf(entry -> !entry.getValue().contains(lemmaId));
        }
        Set<Integer> setKeys = pagesAndHerLemmas.keySet();
        foundPages = indexRepository.descRelevancePages(setKeys, lemmaFromRequestSortAsk);
        return foundPages;
    }

    public List<SearchData> getSearchData(List<Object[]> finalFoundPages) {
        List<SearchData> searchDataList = new ArrayList<>();
        for (Object[] object : finalFoundPages) {
            SearchData searchData = new SearchData();
            String title = null;
            int pageId = (int) object[0];
            BigDecimal bigDecimal = (BigDecimal) object[1];
            double relevance = bigDecimal.doubleValue();
            Page page = pageRepository.findById(pageId);
            String siteAddress = page.getSite().getUrl();
            if (siteAddress.endsWith("/")) {
                siteAddress = siteAddress.substring(0, siteAddress.length() - 1);
            }
            searchData.setSite(siteAddress);
            searchData.setSiteName(page.getSite().getName());
            searchData.setUri(page.getPath());
            searchData.setRelevance(relevance);
            Document pageContent = Jsoup.parse(page.getContent());
            Elements elements = pageContent.getElementsByTag("title");
            for (Element element : elements) {
                title = element.text();
            }
            searchData.setTitle(title);
            searchData.setSnippet(assemblingSnippet(page.getContent()));
            searchDataList.add(searchData);
        }
        return searchDataList;
    }

    public String assemblingSnippet(String content) {
        String withoutTagContent = Jsoup.clean(content, Safelist.none());
        List<String> finalListSnippet = new ArrayList<>();
        String textTemp = withoutTagContent.replaceAll("[-\\n]", " ")
                .replaceAll("[^а-яА-Яa-zA-Z\\s]", " ")
                .replaceAll("(\\b[a-zA-Zа-яА-Я]{1,1}\\b)|(nbsp)", "")
                .replaceAll("\\s+", " ")
                .trim();
        String[] arrayWord = textTemp.split(" ");
        HashSet<String> arrayWordWithoutDoublon = new HashSet<>(Arrays.asList(arrayWord));
        HashMap<String, List<String>> lemmaContentPage = lemmaIndex.getLemmaForSearchPage(arrayWordWithoutDoublon);
        for (String lemmaS : lemmasFromSearchRequest) {
            for (Map.Entry<String, List<String>> item : lemmaContentPage.entrySet()) {
                if (item.getValue().contains(lemmaS)) {
                    finalListSnippet.add(item.getKey());
                }
            }
        }
        return snippetData(content, finalListSnippet);
    }

    public String snippetData(String content, List<String> finalListSnippet) {
        String finalContent = "";
        Document document = Jsoup.parse(content);
        HashSet<String> set1 = new HashSet<>(finalListSnippet);
        for (String finalSnippet : set1) {
            Elements elements = document.select(":matchesOwn(" + finalSnippet + ")");
            for (Element element : elements) {
                finalContent = contentForSnippetData(finalSnippet, finalContent, element);
            }
        }
        for (String finalSnippet : set1) {
            String regWord = "\\b" + finalSnippet + "\\b";
            finalContent = finalContent.replaceAll(regWord, "<b>" + finalSnippet + "</b>");
        }
        return finalContent;
    }

    public String contentForSnippetData(String finalSnippet, String finalContent, Element element) {
        String[] textArray = element.text().split("(\\.+)");
        for (String partOfTheText : textArray) {
            if (partOfTheText.contains(finalSnippet) && !finalContent.contains(partOfTheText)) {
                finalContent = finalContent.concat(".." + partOfTheText + "..");
            }
        }
        return finalContent;
    }

}
