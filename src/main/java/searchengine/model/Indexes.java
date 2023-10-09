package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "`index`", uniqueConstraints = {@UniqueConstraint(name = "UniqueLemmaSiteId", columnNames = {"page_id", "lemma_id"})})
public class Indexes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", insertable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;


    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", insertable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;


    @Column(name = "`rank`")
    private int ranks;

}