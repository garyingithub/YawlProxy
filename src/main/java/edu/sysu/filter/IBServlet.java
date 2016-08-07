package edu.sysu.filter;



import edu.sysu.data.Engine;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.data.YawlService;
import edu.sysu.util.ProxyUtil;
import edu.sysu.util.SessionUtil;
import edu.sysu.util.YawlUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by gary on 16-7-29.
 */
@WebServlet(urlPatterns = "/yawl/ib/*")
public class IBServlet extends BaseServlet{

    private SessionUtil sessionUtil;
    private ProxyUtil proxyUtil;


    String processPostQuery(HttpServletRequest request) {

        String[] folders = request.getRequestURI().split("/");
        String tenant_id_string;
        if (folders.length<=3)
        {
            tenant_id_string="0";
        }
        else
         tenant_id_string = folders[3];


        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String workItemID = request.getParameter("workItemID");
        String specIdentifier = request.getParameter("specidentifier");
        String specVersion = request.getParameter("specversion");
        String specURI = request.getParameter("specuri");
        String taskID = request.getParameter("taskID");
        String userId=request.getParameter("userid");
        String password=request.getParameter("password");

        String caseId=request.getParameter("caseID");
        Specification specification=proxyUtil.getSpecificationByIdAndVersion(specIdentifier+":"+specVersion);

        Tenant tenant= proxyUtil.getTenantById(tenant_id_string);

        if(action!=null)
            switch (action){
                case "connect":
                    msg.append(sessionUtil.connectToProxy(tenant,userId));
                    break;
                case "checkConnection":
                    msg.append(YawlUtil.successMessage(""));
                    break;
                case "disconnect":
                    msg.append("<success/>");
                    break;
                case "getSpecificationPrototypesList":
                    msg.append(tenant.getSpecificationListResponse());
                    break;
                case "getSpecification":
                    if (specification == null) {
                        msg.append(YawlUtil.failureMessage("No specification found for id: " + specIdentifier));
                    }else
                        msg.append(specification.getXML());
                case "launchCase":


                    Engine engine= proxyUtil.getTargetEngine();
                    try {
                        if(tenant.getYawlServices()==null||tenant.getYawlServices().size()==0){
                            YawlService service=new YawlService();
                            service.setName("DefaultWorkList");
                            service.setPassword("resource");
                            service.setUri("http://192.168.199.201:8080/resourceService/ib");
                            service.setTenant(tenant);

                            tenant.addYawlService(service);

                        }
                        msg.append(proxyUtil.launchCase(specification,engine, (YawlService) tenant.getYawlServices().toArray()[0]));
                    } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException | IOException e) {
                   //     e.printStackTrace();
                        msg.append(YawlUtil.failureMessage(e.getMessage()));
                    }
                    break;
                case "startOne":
                    break;
                case "getLiveItems":
                    break;
                case "getAllRunningCases":
                    msg.append(tenant.getAllRunningCasesResponse());
                    break;
                case "cancelCase":
                    edu.sysu.data.Case c=proxyUtil.getCaseById(caseId);
                    msg.append(proxyUtil.cancelCase(c));
                    break;

            }else{
            if(request.getRequestURI().endsWith("ib")){
                msg.append(YawlUtil.successMessage(""));
            }
        }


        return msg.toString();
    }

    public IBServlet(SessionUtil sessionUtil,ProxyUtil proxyUtil){

        this.sessionUtil=sessionUtil;
        this.proxyUtil = proxyUtil;
    }


}
