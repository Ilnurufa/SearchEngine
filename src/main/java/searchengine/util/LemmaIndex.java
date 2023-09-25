package searchengine.util;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;


public class LemmaIndex {

    private Portal portal;
    private Page page;
    private Document document;
    private static LemmaRepository lemmaRepository;
    private static IndexRepository indexRepository;
    public static List<String> dayOfWeek = Arrays.asList("пн", "вт", "ср", "чт", "пт", "сб", "вск", "гб", "вс");
    private static boolean flagAddOnePage = false; // Добавляется ли отдельная страница или идет индексация полная

    public LemmaIndex(Portal portal, Page page, Document document, LemmaRepository lemmaRepository, IndexRepository indexRepository,
                      boolean flagAddOnePage) {
        this.portal = portal;
        this.page = page;
        this.document = document;
        LemmaIndex.lemmaRepository = lemmaRepository;
        LemmaIndex.indexRepository = indexRepository;
        LemmaIndex.flagAddOnePage = flagAddOnePage;
        setLemmas();
    }

    public LemmaIndex() {

    }

    public static List<String> getLemma(String text) {
        List<String> listLemma = new ArrayList<>();
        try {
            LuceneMorphology luceneMorphologyRus = new RussianLuceneMorphology();
            LuceneMorphology luceneMorphologyLatin = new EnglishLuceneMorphology();
            String textTemp = text.replaceAll("[-\\n]", " ")
                    .replaceAll("[^а-яА-Яa-zA-Z\\s]", " ")
                    .replaceAll("(\\b[a-zA-Zа-яА-Я]{1,1}\\b)|(nbsp)", "")
                    .replaceAll("\\s+", " ")
                    .trim(); // .replaceAll("[\\n]", " ")
            String[] wordsArray = textTemp.split(" ");
            for (String elm : wordsArray) {
                if (!elm.equals("") && elm.matches("[а-яА-Я]*")) {
                    listLemma.addAll(getLemmaFromWord(elm, luceneMorphologyRus));
                } else if (!elm.equals("") && !elm.matches("[а-яА-Я]*")) {
                    listLemma.addAll(getLemmaFromWord(elm, luceneMorphologyLatin));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listLemma;
    }

    public static List<String> getLemmaFromWord(String text, LuceneMorphology luceneMorph) {
        List<String> listLemma = new ArrayList<>();
        boolean notTheRightWord = false;
        List<String> wordBaseForms = luceneMorph.getNormalForms(text.toLowerCase());
        for (String s : wordBaseForms) {
            List<String> unionWord = luceneMorph.getMorphInfo(s);
            for (String words : unionWord) {
                if (words.contains("СОЮЗ") || words.contains("ПРЕДЛ") || words.contains(" ЧАСТ") || words.contains("МЕЖД")
                        || dayOfWeek.contains(s) || s.length() == 1 || words.contains("CONJ") || words.contains("INT")
                        || words.contains("PART") || words.contains("PREP")) {
                    notTheRightWord = true;
                    break;
                }
            }
            if (notTheRightWord) {
                break;
            } else {
                listLemma.add(s);
            }
        }
        return listLemma;
    }

    public HashMap<String, List<String>> getLemmaForSearchPage(HashSet<String> text) {
        HashMap<String, List<String>> lemmaContentPage = new HashMap<>();
        try {
            LuceneMorphology luceneMorphologyRus = new RussianLuceneMorphology();
            LuceneMorphology luceneMorphologyLatin = new EnglishLuceneMorphology();
            for (String elm : text) {
                if (!elm.equals("") && elm.matches("[а-яА-Я]*")) {
                    List<String> wordBaseForms = luceneMorphologyRus.getNormalForms(elm.toLowerCase());
                    lemmaContentPage.put(elm, wordBaseForms);
                } else if (!elm.equals("") && elm.matches("[a-zA-Z]*")) {
                    List<String> wordBaseForms = luceneMorphologyLatin.getNormalForms(elm.toLowerCase());
                    lemmaContentPage.put(elm, wordBaseForms);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lemmaContentPage;
    }

    public synchronized void setLemmas() {
        HashMap<String, Integer> lemmas = new HashMap<>();
        List<String> lemmaList = getLemma(Jsoup.clean(document.toString(), Safelist.none()));
        if (!flagAddOnePage) {
            for (String word : lemmaList) {
                lemmas.put(word, (lemmas.containsKey(word)) ? lemmas.get(word) + 1 : 1);
                Lemma lemma = new Lemma();
                lemma.setLemma(word);
                lemma.setFrequency(1);
                lemma.setSite(portal);
                lemmaRepository.save(lemma);
            }
        } else {
            // Добавление\обновление отдельной страницы
            for (String word : lemmaList) {
                lemmas.put(word, (lemmas.containsKey(word)) ? lemmas.get(word) + 1 : 1);
                Lemma lemma1 = lemmaRepository.findByLemmaAndSiteId(word, portal.getId());
                if (lemma1 != null) {
                    lemma1.setFrequency(lemma1.getFrequency() + 1);
                    lemmaRepository.save(lemma1);
                } else {
                    Lemma lemma = new Lemma();
                    lemma.setLemma(word);
                    lemma.setFrequency(1);
                    lemma.setSite(portal);
                    lemmaRepository.save(lemma);
                }
            }
        }
        setIndex(lemmas);

    }

    public synchronized void setIndex(HashMap<String, Integer> hashMap) {
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            Lemma lemma = lemmaRepository.findByLemmaAndSiteId(entry.getKey(), portal.getId());
            Indexes indexes = new Indexes();
            indexes.setLemma(lemma);
            indexes.setPage(page);
            indexes.setRanks(entry.getValue());
            indexRepository.save(indexes);
        }
    }
}
