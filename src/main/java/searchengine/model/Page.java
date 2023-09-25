package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(indexes = {@Index(columnList = "path", name = "path")})
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Portal site;

    private String path;

    //@OneToMany(mappedBy = "page")
    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Indexes> indexess;


    private int code;
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;
}
