package edu.sysu.data;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by gary on 16-7-24.
 */
@Entity
@Table(name="Engines")
public class Engine {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment",strategy = "increment")
    private Long engineId;

    public Long getEngineId() {
        return engineId;
    }

    public void setEngineId(Long engineId) {
        this.engineId = engineId;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public List<Specification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<Specification> specifications) {
        this.specifications = specifications;
    }

    @Column(nullable = false)
    private String url;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "engine")
    List<Case> cases=new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Specification> specifications=new ArrayList<>();

    public void addSpecification(Specification specification){

        specifications.add(specification);
        specification.getEngines().add(this);
    }

    public void removeSpecification(Specification specification){
        specifications.remove(specification);
        specification.getEngines().remove(this);
    }

    public void addCase(Case c){

        cases.add(c);
    }






    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Engine(){

    }


}
