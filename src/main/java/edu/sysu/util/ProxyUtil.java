package edu.sysu.util;

import edu.sysu.data.*;
import edu.sysu.util.HibernateUtil;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.SessionUtil;
import edu.sysu.util.YawlUtil;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */


public class ProxyUtil {

    private Map<String,Object> engines;

    private Map<String,Object> tenants;

    private Map<String ,Object> specifications;

    private Map<String ,Object> cases;

    private Map<String,Object> yawlServices;


    public final String encryptedAdminPassword="Se4tMaQCi9gr0Q2usp7P56Sk5vM=";


    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    private HibernateUtil hibernateUtil;

    public ProxyUtil( RequestForwarder requestForwarder, SessionUtil sessionUtil, HibernateUtil hibernateUtil) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {

        this.requestForwarder = requestForwarder;
        this.sessionUtil = sessionUtil;
        this.hibernateUtil = hibernateUtil;

        try {
            engines= hibernateUtil.getObjectMap("Engine","edu.sysu.data.Engine","getEngineId");
            tenants= hibernateUtil.getObjectMap("Tenant","edu.sysu.data.Tenant","getTenantId");
            specifications=hibernateUtil.getObjectMap("Specification","edu.sysu.data.Specification","getIdAndVersion");
            cases= hibernateUtil.getObjectMap("Case","edu.sysu.data.Case","getEngineIdAndInnerId");
            yawlServices=hibernateUtil.getObjectMap("YawlService","edu.sysu.data.YawlService","getTenantIdAndName");

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // init for testing
        if(tenants.size()==0){
            Tenant tenant=new Tenant();
            tenant.setName("Peter");
            this.storeObject("edu.sysu.data.Tenant","getTenantId","tenants",tenant);

            Tenant tenant1=new Tenant();
            tenant1.setName("Gary");

            this.storeObject("edu.sysu.data.Tenant","getTenantId","tenants",tenant1);

            Engine engine=new Engine();
            engine.setUrl("192.168.199.175:8086");
            this.storeObject("edu.sysu.data.Engine","getEngineId","engines",engine);

            YawlService yawlService=new YawlService();
            yawlService.setName("resourceService");
            yawlService.setUri("192.168.199.175:8086");
            yawlService.setDocument("resource");
            yawlService.setPassword("resource");
            yawlService.setTenant(tenant1);


            this.storeObject("edu.sysu.data.YawlService","getTenantIdAndName","yawlServices",yawlService);


        }

    }


    public Tenant getTenantByCaseId(String caseId){
        Case c= this.getCaseByEngineIdAndInnerId(caseId);

        return c.getSpecification().getTenant();
    }

    public Tenant getTenantBySpecificationNaturalId(String specificationNaturalId){
        Specification specification= (Specification) this.specifications.get(specificationNaturalId);
        return specification.getTenant();
    }

    public Tenant getTenantById(String tenantId){
        return (Tenant) this.tenants.get(tenantId);
    }



    private Map getEngines(){
        return this.engines;
    }

    public Engine getTargetEngine(){
        return (Engine) engines.values().toArray()[0];
    }


    public Engine getEngineById(String engineId)
    {
        return (Engine) this.getEngines().get(engineId);
    }


    public Specification getSpecificationByIdAndVersion(String idAndVersion){
        logger.debug(String.format("get specification by %s",idAndVersion));
        return (Specification) specifications.get(idAndVersion);
    }



    public YawlService getYawlServiceByTenantIdAndName(String tenantIdAndName){
        return (YawlService) this.yawlServices.get(tenantIdAndName);
    }



    public Case getCaseByEngineIdAndInnerId(String EngineIdAndInnerId){
       Case c=  (Case) cases.get(EngineIdAndInnerId);
        // in case that the announceCaseStart arrive before launchCase finish
        while (c==null){
            c= (Case) cases.get(EngineIdAndInnerId);
        }

        return c;
    }

    public void storeObject(String  className,String idName,String rpFieldName,Object object) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

        hibernateUtil.storeObject(object);
        Map<String,Object> map= (Map<String, Object>) this.getClass().getDeclaredField(rpFieldName).get(this);
        map.put(Class.forName(className).getDeclaredMethod(idName).invoke(object).toString(),object);

        logger.debug("store an %s ",className);


    }

    private RequestForwarder requestForwarder;
    private SessionUtil sessionUtil;
    private void uploadSpecification(Specification specification,Engine engine) throws IOException {
        Map<String,String> params=new HashMap<>();
        params.put("action","upload");
        params.put("sessionHandle",sessionUtil.getAdminSession(engine));
        params.put("specXML",specification.getXML());

        String result="";

        result=requestForwarder.forwardRequest(engine.getUrl()+"/yawl/ia",params);

        if(result.contains("success"))
            return;
        if(result.contains("failure")&&result.contains("warning"))
            return;

        //throw new IOException(result);




    }

    public String launchCase(Specification specification,Engine engine) throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {

        uploadSpecification(specification,engine);

        String sessionHandle=sessionUtil.getAdminSession(engine);
        Map<String,String> params=new HashMap<>();
        String specId=specification.getSpecId();
        String specVersion=specification.getSpecVersion();

        params.put("action","launchCase");
        params.put("specidentifier",specId);
        params.put("logData","<value>resourceService</value><descriptor>launched</descriptor><datatype>string</datatype><datatypedefinition>string</datatypedefinition></logdataitem></logdataitemlist>");
        params.put("sessionHandle",sessionHandle);
        params.put("specuri",specification.getSpecificationUri());
        params.put("specversion",specVersion);


        String result="";

        result=requestForwarder.forwardRequest(engine.getUrl()+"/yawl/ib",params);

        if(result.contains("failure")){
            throw new IOException("can't launch case");
        }
        String caseInnerId=result;


        Case c=new Case();
        c.setInnerId(caseInnerId);
        c.setSpecification(specification);
        c.setEngine(engine);
        c.setOuterId(caseInnerId);

        specification.addCase(c);
        engine.addCase(c);
        try {
            this.storeObject("edu.sysu.data.Case","getEngineIdAndInnerId","cases",c);
            hibernateUtil.updateObject(engine);
            hibernateUtil.updateObject(specification);
        }catch (Exception ex){
            return ex.getMessage();
        }



        return result;

    }


    public String loadSpecification(String specXML, Tenant tenant) {

        YVerificationHandler verificationHandler = new YVerificationHandler();
        List<YSpecification> newSpecifications = null;
        try {
            newSpecifications = YMarshal.unmarshalSpecifications(specXML);
        } catch (YSyntaxException e) {
            e.printStackTrace();
        }

        if (newSpecifications != null) {
            for (YSpecification specification : newSpecifications) {
                specification.verify(verificationHandler);
                if (verificationHandler.hasErrors()) {
                    String errDetail = specification.getSchemaVersion().isBetaVersion() ?
                            "URI: " + specification.getURI() : "UID: " + specification.getID();
                    errDetail += "- Version: " + specification.getSpecVersion();
                    return ("There is a specification with an identical id to ["
                            + errDetail + "] already loaded into the engine.");
                } else {
                    Specification spec = new Specification();
                    spec.setIdAndVersion(specification.getID() +":"+ specification.getSpecVersion());
                    spec.setXML(YMarshal.marshal(specification));
                    spec.setTenant(tenant);
                    spec.setSpecificationUri(specification.getURI());
                    tenant.addSpecification(spec);
                    hibernateUtil.updateObject(tenant);
                }
            }
        }
        return "";

    }














}
