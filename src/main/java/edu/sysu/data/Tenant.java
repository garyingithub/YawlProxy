package edu.sysu.data;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import edu.sysu.data.YawlService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by gary on 16-7-24.
 */
@Entity
@Table( name="Tenants")
public class Tenant {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment",strategy = "increment")
    private Long tenantId;

    @NaturalId
    @Column(unique = true)
    private String name;

    @OneToMany(cascade = {CascadeType.ALL} ,mappedBy = "tenant")
    private List<YawlService> yawlServices=new ArrayList<>();

    @OneToMany(cascade = {CascadeType.ALL} ,mappedBy = "tenant",fetch = FetchType.EAGER)
    private List<Specification> specifications=new ArrayList<>();



    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Specification> getSpecifications() {
        return specifications;
    }

    public void addSpecification(Specification specification){
        // need to lock the specifications

        this.specifications.add(specification);
    }

    public void deleteSpecification(Specification specification){
        this.specifications.remove(specification);
    }

    public Tenant(){

    }


}
