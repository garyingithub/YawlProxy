package edu.sysu.filter;

import edu.sysu.util.ProxyUtil;
import edu.sysu.data.Tenant;
import edu.sysu.data.YawlService;
import edu.sysu.util.RequestForwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gary on 16-7-31.
 */
@WebServlet(urlPatterns = "/resourceService/*")
public class ResourceServiceServlet extends HttpServlet{

    public Logger logger= LoggerFactory.getLogger(this.getClass());
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info(req.getRequestURI());
        logger.info(req.getMethod());
        logger.info(req.getParameter("action"));

        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(resp);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(req));
        output.append("</response>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();




    }

    ProxyUtil proxyUtil;
    RequestForwarder requestForwarder;
    public String processPostQuery(HttpServletRequest request){
        String[] folders = request.getRequestURI().split("/");
        String engine_id_string = "";
        if (folders.length<=3)
        {
            engine_id_string="1";
        }
        else
            engine_id_string = folders[3];

        String action = request.getParameter("action");
        String caseID = request.getParameter("caseID");
        String workItemXML = request.getParameter("workItem");
        String specificationId=request.getParameter("specidentifier");
        String specVersion=request.getParameter("specversion");
        String caseId=request.getParameter("caseId");


        Tenant tenant=null;

        WorkItemRecord workItem = (workItemXML != null) ?
                Marshaller.unmarshalWorkItem(workItemXML) : null;
        if(workItemXML!=null){
            caseId=workItem.getRootCaseID();

        }

        if(caseId!=null){
            tenant= proxyUtil.getTenantByCaseId(engine_id_string+":"+caseId);
        }

        if(specificationId!=null){
            tenant= proxyUtil.getTenantBySpecificationNaturalId(specificationId+":"+specVersion);
        }

        if(tenant==null){
            return "";
        }

        YawlService yawlService= proxyUtil.getYawlServiceByTenantIdAndName(tenant.getTenantId()+":"+"resourceService");

        Map<String,String> params=new HashMap<>();

        Enumeration<String> enumeration=request.getParameterNames();
        while (enumeration.hasMoreElements()){
            String paramName=enumeration.nextElement();
            if(paramName.equals("caseID")){
                params.put("caseID", proxyUtil.getCaseByEngineIdAndInnerId(engine_id_string+":"+caseId).getCaseId().toString());
            }else
                params.put(paramName,request.getParameter(paramName));
        }


        String response="";
        try {
            response=requestForwarder.forwardRequest(yawlService.getUri()+"/resourceService/"+tenant.getTenantId()+"/ib",params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public ResourceServiceServlet(ProxyUtil proxyUtil, RequestForwarder requestForwarder) {
        this.proxyUtil = proxyUtil;
        this.requestForwarder = requestForwarder;
    }
}
