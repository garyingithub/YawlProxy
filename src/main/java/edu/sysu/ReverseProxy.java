package edu.sysu;

import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import edu.sysu.data.Case;
import edu.sysu.data.Engine;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.YawlUtil;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */


public class ReverseProxy {


    private Map<String,Object> engines;


    private Map<String,Object> tenants;


    private Map<String ,Object> specifications;


    private Map<String ,Object> cases;


    public final String encryptedAdminPassword;

    public final String encryptedDefaultWorklistPassword;

    HibernateUtil hibernateUtil;
    YawlUtil yawlUtil;


    public ReverseProxy(HibernateUtil util,YawlUtil yawlUtil){
        this.hibernateUtil=util;
        this.yawlUtil=yawlUtil;
        engines= hibernateUtil.getObjectMap("Engine","edu.sysu.data.Engine","getEngineId");
        tenants= hibernateUtil.getObjectMap("Tenant","edu.sysu.data.Tenant","getTenantId");
        specifications=hibernateUtil.getObjectMap("Specification","edu.sysu.data.Specification","getSpecicationId");
        cases= hibernateUtil.getObjectMap("Case","edu.sysu.data.Case","getCaseId");

        if(tenants.size()==0){
            Tenant tenant=new Tenant();
            tenant.setName("Peter");
            hibernateUtil.storeObject(tenant);

            Tenant tenant1=new Tenant();
            tenant.setName("Gary");
            hibernateUtil.storeObject(tenant1);

            Engine engine=new Engine();
            engine.setUrl("222.200.180.59:30004");
            hibernateUtil.storeObject(engine);
            engines= hibernateUtil.getObjectMap("Engine","edu.sysu.data.Engine","getEngineId");
            tenants= hibernateUtil.getObjectMap("Tenant","edu.sysu.data.Tenant","getTenantId");


        }

        String a="";
        String b="";
        try {
            a=yawlUtil.encrypt("YAWL");
            b=yawlUtil.encrypt("resource");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        encryptedAdminPassword=a;
        encryptedDefaultWorklistPassword=b;

    }

    public Tenant getTenant(String tenantId){
        return (Tenant) this.tenants.get(tenantId);
    }

    public Map getEngines(){
        return this.engines;
    }

    public String getSpecificationList(){

        List<Specification> specificationList=hibernateUtil.getAllObjects("Specification");
        Set<YSpecification> result=new HashSet<>();
        for(int i=0;i<specificationList.size();i++){
            List<YSpecification> temp=new ArrayList<>();
            try {
                 temp=YMarshal.unmarshalSpecifications(specificationList.get(i).getXML());

            } catch (YSyntaxException e) {
                e.printStackTrace();
            }
            for(int j=0;j<temp.size();j++){
                result.add(temp.get(j));
            }
        }
        return yawlUtil.getDataForSpecifications(result);
    }











}
