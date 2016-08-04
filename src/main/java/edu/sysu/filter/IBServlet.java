package edu.sysu.filter;



import edu.sysu.data.Engine;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
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
                        msg.append(proxyUtil.launchCase(specification,engine));
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
