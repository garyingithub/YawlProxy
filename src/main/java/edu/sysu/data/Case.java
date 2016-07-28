package edu.sysu.data;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.Objects;

/**
 * Created by gary on 16-7-26.
 */
@Entity
@Table(name="Cases")
public class Case {


    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment",strategy = "increment")
    private Long caseId;

    @NaturalId
    @Column(unique = true,nullable = false)
    private String outerId;

    private String innerId;

    @ManyToOne
    @JoinColumn(name="specificationId")
    private Specification specification;

    @ManyToOne
    private Engine engine;

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof Case ) ) {
            return false;
        }
        Case c = (Case) o;
        return Objects.equals( outerId, c.outerId );
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.getOuterId());
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }


    public String getInnerId() {
        return innerId;
    }

    public void setInnerId(String innerId) {
        this.innerId = innerId;
    }

    public String getOuterId() {
        return outerId;
    }

    public void setOuterId(String outerId) {
        this.outerId = outerId;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
