package edu.sysu.data;

import edu.sysu.util.YawlUtil;
import org.hibernate.annotations.NaturalId;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.XNode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gary on 16-7-24.
 */
@Entity
@Table( name="Tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //@GenericGenerator(name = "increment",strategy = "increment")
    private Long tenantId;

    @NaturalId
    @Column(unique = true)
    private String name;

    @OneToMany(cascade = {CascadeType.ALL} ,mappedBy = "tenant",fetch = FetchType.EAGER)
    private Set<YawlService> yawlServices=new HashSet<>();

    @OneToMany(cascade = {CascadeType.ALL} ,mappedBy = "tenant",fetch = FetchType.EAGER)
    private List<Specification> specifications=new ArrayList<>();

    public Set<YawlService> getYawlServices() {
        return yawlServices;
    }

    public void addYawlService(YawlService yawlService){
        this.yawlServices.add(yawlService);
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Specification getSpecification(Specification specification){
        for(Specification s:this.getSpecifications()){
            if(s.equals(specification))
                return s;
        }
        return null;
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

    public Tenant(String name) {
        this.name = name;
    }

    public void addSpecification(Specification specification){
        // need to lock the specifications

        if(this.specifications==null)
            specifications=new ArrayList<>();

        this.specifications.add(specification);
    }

    public void deleteSpecification(Specification specification){
        this.specifications.remove(specification);
    }

    public Tenant(){

    }

    @Transient
    public String getSpecificationListResponse(){

        List<Specification> specificationList=this.getSpecifications();
        Set<YSpecification> result=new HashSet<>();
        for (Specification aSpecificationList : specificationList) {
            List<YSpecification> temp = new ArrayList<>();
            try {
                temp = YMarshal.unmarshalSpecifications(aSpecificationList.getXML());
            } catch (YSyntaxException e) {
                e.printStackTrace();
            }
            result.addAll(temp);
        }
        return YawlUtil.getDataForSpecifications(result);
    }

    @Transient
    public String getYawlServicesResponse(){
        StringBuilder builder=new StringBuilder();
        for(YawlService yawlService:this.getYawlServices()){
            XNode root = new XNode("yawlService");
            root.addAttribute("id", yawlService.getUri());
            if (yawlService.getDocument() != null) {
                root.addChild("documentation", yawlService.getDocument() );
            }
            root.addChild("servicename", yawlService.getName());
            root.addChild("servicepassword", yawlService.getPassword());
            root.addChild("assignable", true);
            builder.append(root.toString());
        }
        return builder.toString();
    }


    @Transient
    public String getAllRunningCasesResponse(){


        XNode node = new XNode("AllRunningCases");
        for (Specification specID : getSpecifications()) {

            XNode idNode = node.addChild("specificationID");
            idNode.addAttribute("identifier", specID.getSpecId());
            idNode.addAttribute("version", specID.getSpecVersion());
            idNode.addAttribute("uri", specID.getSpecificationUri());
            for (edu.sysu.data.Case caseID : specID.getCases()) {
                idNode.addChild("caseID", caseID.getCaseId());
            }
        }
        return node.toString();
    }
}
