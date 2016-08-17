package edu.sysu.util;

import edu.sysu.cache.*;
import edu.sysu.data.*;

import edu.sysu.util.HibernateUtil;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.SessionUtil;
import edu.sysu.util.YawlUtil;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */


public class ProxyUtil {

    private RequestForwarder requestForwarder;
    private SessionUtil sessionUtil;
    public CaseCache caseCache;
    public SpecificationCache specificationCache;
    public YawlServiceCache yawlServiceCache;
    public TenantCache tenantCache;
    public EngineCache engineCache;

    public static final String encryptedAdminPassword = "Se4tMaQCi9gr0Q2usp7P56Sk5vM=";


    public ProxyUtil(EngineCache engineCache, TenantCache tenantCache, RequestForwarder requestForwarder, SessionUtil sessionUtil, CaseCache caseCache, SpecificationCache specificationCache, YawlServiceCache yawlServiceCache) {
        this.requestForwarder = requestForwarder;
        this.sessionUtil = sessionUtil;
        this.caseCache = caseCache;
        this.specificationCache = specificationCache;
        this.yawlServiceCache = yawlServiceCache;
        this.tenantCache = tenantCache;
        this.engineCache = engineCache;


        // init for testing
        if (tenantCache.getSize() == 0) {
            Tenant tenant = new Tenant();
            tenant.setName("Peter");
            tenantCache.storeTenant(tenant);

            Tenant tenant1 = new Tenant();
            tenant1.setName("Gary");

            tenantCache.storeTenant(tenant1);


            Engine engine = new Engine();
            engine.setUrl("http://localhost:8086");

            engineCache.storeEngine(engine);

            Engine engine1 = new Engine();
            engine1.setUrl("http://localhost:8087");
            engineCache.storeEngine(engine1);

            YawlService yawlService = new YawlService();
            yawlService.setName("resourceService");
            yawlService.setUri("http://localhost:8086/resourceService/ib");
            yawlService.setDocument("resource");
            yawlService.setPassword("resource");
            yawlService.setTenant(tenant1);


            yawlServiceCache.storeYawlService(yawlService);

        }
    }

    private void registerYawlService(YawlService yawlService, Engine engine) throws IOException {
        YAWLServiceReference serviceReference = new YAWLServiceReference(yawlService.getUri(),
                null, yawlService.getName(), yawlService.getDocument());
        Map<String, String> params = new HashMap<>();
        params.put("action", "newYAWLService");
        params.put("sessionHandle", sessionUtil.getAdminSession(engine));
        params.put("service", serviceReference.toXMLComplete());
        String result = requestForwarder.forwardRequest(engine.getIAUri(), params);
        if (!result.contains("already") && result.contains("failure")) {
            throw new IOException(result);
        }
    }


    private void uploadSpecification(Specification specification, Engine engine) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("action", "upload");
        params.put("sessionHandle", sessionUtil.getAdminSession(engine));
        params.put("specXML", specification.getXML());


        String result = "";

        result = requestForwarder.forwardRequest(engine.getUrl() + "/yawl/ia", params);

        if (result.contains("success"))
            return;
        if (result.contains("failure") && result.contains("warning"))
            return;

        //throw new IOException(result);
        
    }

    public String launchCase(Specification specification, Engine engine, YawlService yawlService) throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {

        uploadSpecification(specification, engine);

        registerYawlService(yawlService, engine);

        String sessionHandle = sessionUtil.connectToEngineAsService(engine, yawlService);
        Map<String, String> params = new HashMap<>();
        String specId = specification.getSpecId();
        String specVersion = specification.getSpecVersion();

        params.put("action", "launchCase");
        params.put("specidentifier", specId);
        params.put("logData", "<value>resourceService</value><descriptor>launched</descriptor><datatype>string</datatype><datatypedefinition>string</datatypedefinition></logdataitem></logdataitemlist>");
        params.put("sessionHandle", sessionHandle);
        params.put("specuri", specification.getSpecificationUri());
        params.put("specversion", specVersion);
        params.put("completionObserverURI",yawlService.getUri());

        String result = "";

        result = requestForwarder.forwardRequest(engine.getUrl() + "/yawl/ib", params);

        if (result.contains("failure")) {
            throw new IOException("can't launch case");
        }
        String caseInnerId = result;


        Case c = new Case();
        c.setInnerId(caseInnerId);
        c.setSpecification(specification);
        tenantCache.getTenantById(specification.getTenant().getTenantId().toString()).
                getSpecification(specification).addCase(c);
        c.setEngine(engine);
        specification.addCase(c);
        engine.addCase(c);
        caseCache.storeCase(c);


        return result + "~" + engine.getIBUri() + "~" + sessionHandle;

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
                    for(YSpecification s:newSpecifications){
                        for(YDecomposition decomposition:s.getDecompositions()){
                            if(decomposition instanceof YNet)
                                continue;

                            YAWLServiceGateway yawlServiceGateway=(YAWLServiceGateway)decomposition;
                            YawlService yawlService;
                            if(yawlServiceGateway.getYawlService()!=null){
                                YAWLServiceReference reference=yawlServiceGateway.getYawlService();
                                yawlService=yawlServiceCache.getYawlServiceByUri(reference.getURI());
                                reference.set_yawlServiceID(yawlService.getUri());
                            }else{
                                /*
                                yawlService=yawlServiceCache.getYawlServiceByUri(tenant.getTenantId()+":"+"resourceService");
                                YAWLServiceReference yawlServiceReference=new YAWLServiceReference();
                                yawlServiceReference.set_assignable(true);
                                yawlServiceReference.set_yawlServiceID(yawlService.getUri());
                                yawlServiceReference.set_serviceName(yawlService.getName());
                                yawlServiceReference.set_servicePassword(yawlService.getPassword());
                                yawlServiceReference.set_documentation(yawlService.getDocument());
                                yawlServiceGateway.setYawlService(yawlServiceReference);
                                */
                            }

                        }
                    }
                    Specification spec = new Specification();
                    spec.setIdAndVersion(specification.getID() + ":" + specification.getSpecVersion());
                    spec.setXML(YMarshal.marshal(specification));
                    spec.setTenant(tenant);
                    spec.setSpecificationUri(specification.getURI());
                    try {
                        specificationCache.storeSpecification(spec);
                    } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    tenant.addSpecification(spec);

                }
            }
        }
        return "";

    }

    public String cancelCase(edu.sysu.data.Case c) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "cancelCase");
        params.put("caseID", c.getInnerId());

        String result = "";
        try {
            params.put("sessionHandle", sessionUtil.getAdminSession(c.getEngine()));
            result = requestForwarder.forwardRequest(c.getEngine().getIBUri(), params);
        } catch (IOException e) {
            return YawlUtil.failureMessage(e.getMessage());
        }
        return result;
    }

    public String addYawlService(YAWLServiceReference serviceReference,Tenant tenant){
        if (null != serviceReference) {
            if (null==yawlServiceCache.getYawlServiceByUri(serviceReference.getURI())) {
                if (HttpURLValidator.validate(serviceReference.getURI()).startsWith("<success")) {

                    YawlService service=YawlService.transformFromReference(serviceReference);
                    service.setTenant(tenant);
                    tenant.addYawlService(service);
                    yawlServiceCache.storeYawlService(service);
                    return "<success/>";

                }
                else {
                    return YawlUtil.failureMessage("Service unresponsive: " + serviceReference.getURI());
                }
            } else {
                return YawlUtil.failureMessage("Engine has already registered a service with " +
                        "the same URI [" + serviceReference.getURI() + "]");
            }
        } else {
            return YawlUtil.failureMessage("");
        }
    }

    public String getTaskInformation(Specification specification,String taskId){


        YTask task = YawlUtil.getTaskDefinition(specification,taskId);
        if (task != null) {
            return task.getInformation();
        } else {
            return YawlUtil.failureMessage("The was no task found with ID " + taskId);
        }
    }


}
