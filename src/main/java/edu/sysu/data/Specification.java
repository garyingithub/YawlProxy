package edu.sysu.data;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import edu.sysu.data.Case;
/**
 * Created by gary on 16-7-22.
 */
@Entity
@Table(name="Specifications")
public class Specification {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name="increment",strategy = "increment")
    private Long specicationId;

    // uuid
    @NaturalId
    @Column(unique = true,nullable = false)
    private String IdAndVersion;

    @org.hibernate.annotations.Type( type = "text" )
    private String XML;

    @ManyToOne(fetch = FetchType.EAGER)
    private Tenant tenant;

    @OneToMany(cascade = {CascadeType.ALL} ,mappedBy = "specification",fetch = FetchType.EAGER)
    private List<edu.sysu.data.Case> cases=new ArrayList<>();

    @ManyToMany(mappedBy = "specifications")
    private List<Engine> engines=new ArrayList<>();

    public List<Case> getCases() {
        return cases;
    }

    public void addCase(edu.sysu.data.Case c){
        this.cases.add(c);
    }

    public String getSpecificationUri() {
        return specificationUri;
    }

    public void setSpecificationUri(String specificationUri) {
        this.specificationUri = specificationUri;
    }

    private String specificationUri;

    public Long getSpecicationId() {
        return specicationId;
    }

    public void setSpecicationId(Long specicationId) {
        this.specicationId = specicationId;
    }

    public String getSpecId()
    {
        return this.getIdAndVersion().split(":")[0];
    }

    public String getSpecVersion(){
        return this.getIdAndVersion().split(":")[1];
    }

    public String getXML() {
        return XML;
    }

    public void setXML(String XML) {
        this.XML = XML;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getIdAndVersion() {
        return IdAndVersion;
    }

    public void setIdAndVersion(String idAndVersion) {
        IdAndVersion = idAndVersion;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }






    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof Case ) ) {
            return false;
        }
        Specification c = (Specification) o;
        return Objects.equals( getIdAndVersion(), c.getIdAndVersion() );
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.getIdAndVersion());
    }

    public List<Engine> getEngines(){
        return this.engines;
    }




    public String getCasesForSpecificationResponse(){
        StringBuilder builder=new StringBuilder();
        for(edu.sysu.data.Case c:this.getCases()){
            builder.append("<caseID>");
            builder.append(c.getCaseId().toString());
            builder.append("</caseID>");
        }
        return builder.toString();
    }






}
