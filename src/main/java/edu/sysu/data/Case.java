package edu.sysu.data;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.yawlfoundation.yawl.util.XNode;

import javax.persistence.*;
import java.util.Objects;

/**
 * Created by gary on 16-7-26.
 */
@Entity
@Table(name = "Cases")
public class Case {


    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment",strategy = "increment")
    private Long caseId;

    private String innerId;

    @ManyToOne(fetch = FetchType.EAGER)
    private Specification specification;

    @ManyToOne(fetch = FetchType.EAGER)
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
        return Objects.equals( caseId, c.caseId );
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.caseId);
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

    public String getEngineIdAndInnerId(){
        return engine.getEngineId()+":"+getInnerId();
    }

    public String getSpecificationForCaseResponse(){
        return this.getSpecification().getXML();
    }

    public String getSpecificationIDForCaseResponse(){
        Specification specification=this.getSpecification();

        XNode node = new XNode("specificationid");
        if (specification.getSpecId() != null) {
            node.addChild("identifier", specification.getSpecId());
        }
        node.addChild("version", specification.getSpecVersion());
        node.addChild("uri", specification.getSpecificationUri());

        return node.toString();
    }
}
