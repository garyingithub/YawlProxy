package edu.sysu.filter;

import edu.sysu.data.Engine;
import edu.sysu.data.Tenant;
import edu.sysu.data.YawlService;
import edu.sysu.util.ProxyUtil;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.YawlUtil;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */
@WebServlet(urlPatterns = "/resourceService/*")
public class ResourceServiceServlet extends BaseServlet{

    private ProxyUtil proxyUtil;
    private RequestForwarder requestForwarder;
    public String processPostQuery(HttpServletRequest request){
        String[] folders = request.getRequestURI().split("/");
        String engine_id_string;
        if (folders.length<=3)
        {
            engine_id_string="1";
        }
        else
            engine_id_string = folders[3];

        String action = request.getParameter("action");

        String specificationId=request.getParameter("specidentifier");
        String specVersion=request.getParameter("specversion");
        String caseId=request.getParameter("caseID");
        String workItem=request.getParameter("workItem");
        if(workItem!=null){
            WorkItemRecord workItemRecord=Marshaller.unmarshalWorkItem(workItem);
            caseId=workItemRecord.getRootCaseID();
        }


        Tenant tenant=null;
        Engine engine=proxyUtil.engineCache.getEngineById(engine_id_string);


        if(caseId!=null){
            tenant= proxyUtil.caseCache.getTenantByCaseId(engine_id_string+":"+caseId);
        }

        if(tenant==null&&specificationId!=null){
            tenant= proxyUtil.specificationCache.getTenantBySpecificationNaturalId(specificationId+":"+specVersion);
        }

        if(tenant==null){
            return YawlUtil.successMessage("");
        }

        Set<YawlService> yawlServiceSet=tenant.getYawlServices();

        Map<String,String> params=new HashMap<>();


        Enumeration<String> enumeration=request.getParameterNames();
        while (enumeration.hasMoreElements()){
            String paramName=enumeration.nextElement();
            if(paramName.equals("caseID")){
                if(action!=null && action.equals("announceCaseStarted")){
                    params.put("caseID", caseId+"~"+engine.getIBUri());
                }
                else
                    params.put("caseID", proxyUtil.caseCache.getCaseByEngineIdAndInnerId(
                        engine_id_string+":"+caseId).
                        getCaseId().
                        toString());
            }else
                params.put(paramName,request.getParameter(paramName));
        }


        edu.sysu.data.Case c=proxyUtil.caseCache
                .getCaseByEngineIdAndInnerId(engine_id_string+":"+caseId);
        String response="";
        try {
            for(YawlService yawlService:yawlServiceSet){
                response=requestForwarder.forwardRequest(yawlService.getUri(),params);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(action!=null){

            switch (action){
                case "announceCaseCompleted":

                    if(c!=null)
                        proxyUtil.caseCache.deleteCase(c);
                    break;


            }
        }
        return response;
    }

    public ResourceServiceServlet(ProxyUtil proxyUtil, RequestForwarder requestForwarder) {
        this.proxyUtil = proxyUtil;
        this.requestForwarder = requestForwarder;
    }
}
