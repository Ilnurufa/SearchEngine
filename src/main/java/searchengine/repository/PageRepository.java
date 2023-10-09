package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;

import java.util.ArrayList;

@Transactional
@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    @Modifying
    @Query(value = "DELETE FROM `search_engine`.`page` WHERE id = :pageId", nativeQuery = true)
    void deletePage(int pageId);

    Page findByPath(String path);

    int countAllBySiteId(int siteId);

    Page findById(int pageId);
}
