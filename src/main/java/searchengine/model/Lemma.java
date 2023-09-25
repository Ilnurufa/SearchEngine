package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@SQLInsert(sql = "INSERT INTO lemma(frequency, lemma, site_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE frequency = frequency + 1" )
@Table(name = "lemma", uniqueConstraints = { @UniqueConstraint(name = "UniqueLemmaSiteId", columnNames = { "site_id", "lemma" }) })
//@Table(name = "lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private String lemma;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Portal site;


    private int frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.MERGE)
    private List<Indexes> indexes;


}

