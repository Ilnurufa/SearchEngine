package searchengine.model;

import org.apache.catalina.LifecycleState;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "site")
public class Portal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status anEnum;

    @Column(name = "status_time")
    private Date statusTime;

    private String lastError;
    private String url;
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Page> pages;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE, orphanRemoval = true)
//    @OneToMany(mappedBy = "site")
    private List<Lemma> lemmas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getAnEnum() {
        return anEnum;
    }

    public void setAnEnum(Status anEnum) {
        this.anEnum = anEnum;
    }

}
