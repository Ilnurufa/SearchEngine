package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Portal;

import java.util.ArrayList;

@Transactional
@Repository
public interface SiteRepository extends CrudRepository<Portal, Integer> {

    @Modifying
    @Query(value = "ALTER TABLE `search_engine`.`site` AUTO_INCREMENT = 0",nativeQuery = true)
    void resetIndex();

    @Modifying
    @Query(value = "DELETE FROM `site` WHERE name = :name", nativeQuery = true)
    void deleteByName(String name);

    @Modifying
    @Query(value = "UPDATE `site` SET status = 'INDEXED' WHERE id = :id", nativeQuery = true)
    void updateStatus(int id);

    @Modifying
    @Query(value = "UPDATE `site` SET status = 'FAILED', last_error = 'Индексация остановлена пользователем' " +
            "WHERE id = :id", nativeQuery = true)
    void updateStatusStopIndexing(int id);

    @Modifying
    @Query(value = "SELECT id FROM `site` WHERE status = 'INDEXING'", nativeQuery = true)
    ArrayList<Integer> findStatusIndexing();

    Portal findByUrl(String name);
}
