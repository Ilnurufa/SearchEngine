package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Indexes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public interface IndexRepository extends CrudRepository<Indexes, Integer> {

    ArrayList<Indexes> findByLemmaId(int id);

    @Query(value = "SELECT page_id FROM `index` WHERE lemma_id = :firstLemmaId", nativeQuery = true)
    ArrayList<Integer> foundPages(int firstLemmaId);

    @Query(value = "SELECT lemma_id FROM `index` WHERE page_id = :pageId", nativeQuery = true)
    ArrayList<Integer> findLemmasInPage(int pageId);

    @Query(value = "SELECT page_id, SUM(`rank`)/MAX(SUM(`rank`)) OVER() AS rel FROM `index` " +
            "Where page_id IN :foundPages AND lemma_id IN :foundLemmas " +
            "GROUP BY page_id ORDER BY rel DESC", nativeQuery = true)
    ArrayList<Object[]> descRelevancePages(@Param("foundPages") Set<Integer> pageId, @Param("foundLemmas")List<Integer> lemmaId);


}
