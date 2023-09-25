package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;

import java.util.ArrayList;
import java.util.HashSet;

@Transactional
@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Modifying
    @Query(value = "INSERT INTO lemma(frequency, lemma, site_id) VALUES (1,'интернет',1) ON DUPLICATE KEY UPDATE `frequency` = `frequency` + 1", nativeQuery = true)
    void insertLemma(String result);


    Lemma findByLemmaAndSiteId(String lemma, int siteId);

    @Modifying
    @Query(value = "UPDATE `search_engine`.`lemma` AS l " +
            "JOIN `search_engine`.`index` AS i ON l.id = i.lemma_id " +
            "SET l.frequency = (l.frequency - i.`rank`) " +
            "Where i.page_id = :pageId", nativeQuery = true)
    void updateFrequency(int pageId);

    int countAllBySiteId(int siteId);

    @Query(value = "SELECT id FROM lemma WHERE lemma IN :lemmaSearch AND site_id = :siteId ORDER BY frequency ASC", nativeQuery = true)
    ArrayList<Integer> lemmaByAsc(@Param("lemmaSearch")HashSet<String> lemmaSearch, int siteId);

    @Query(value = "SELECT id FROM lemma WHERE lemma IN :lemmaSearch ORDER BY frequency ASC", nativeQuery = true)
    ArrayList<Integer> lemmaByAskWithoutSiteId(@Param("lemmaSearch")HashSet<String> lemmaSearch);

    @Query(value = "SELECT site_id FROM lemma WHERE lemma IN :lemmaSearch ORDER BY frequency ASC", nativeQuery = true)
    HashSet<Integer> siteIdWhereLemmaFound(@Param("lemmaSearch")HashSet<String> lemmaSearch);


}
