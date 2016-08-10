package edu.sysu.filter;



import edu.sysu.util.ProxyUtil;
import edu.sysu.data.Tenant;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.SessionUtil;
import edu.sysu.util.YawlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;

/**
 * Created by gary on 16-7-29.
 */
@WebServlet(urlPatterns = "/yawl/ia/*")
public class IAServlet extends BaseServlet{

    private SessionUtil sessionUtil;
    private ProxyUtil proxyUtil;


    String processPostQuery(HttpServletRequest request){
        String[] folders = request.getRequestURI().split("/");
        String tenant_id_string="";
        if (folders.length<=3)
        {
            tenant_id_string="0";
        }
        else
             tenant_id_string = folders[3];

        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String userID = request.getParameter("userID");
        String password = request.getParameter("password");

        Tenant tenant=null;
        if(tenant_id_string!=null){
            tenant= proxyUtil.tenantCache.getTenantById(tenant_id_string);
        }


        if(action!=null)
        switch (action){
            case "connect":
                msg.append(sessionUtil.connectToProxy(tenant,userID));
                break;
            case "disconnect":
            case "checkConnection":
                msg.append(YawlUtil.successMessage(""));
                break;
            case "upload":
                String specXML=request.getParameter("specXML");

                String result= proxyUtil.loadSpecification(specXML,tenant);
                if(result.isEmpty()){
                    msg.append(YawlUtil.successMessage(""));
                }
                else {
                    msg.append(YawlUtil.failureMessage(result));
                }
                break;
            case "getList":
                msg.append(tenant.getSpecificationListResponse());
                break;
            case "getYAWLServices":
                msg.append(tenant.getYawlServicesResponse());
                break;
            case "getPassword":
                if(userID.equals("admin"))
                    msg.append(proxyUtil.encryptedAdminPassword);
                else if(userID.equals("editor"))
                    msg.append("VfrZ/SW35S1ytFXq9Giw7+A05wA=");
                break;
            case "getBuildProperties":
                msg.append(YawlUtil.getBuildProperties());
                break;

        }

        return msg.toString();
    }

    public IAServlet(SessionUtil sessionUtil, ProxyUtil proxyUtil ){

        this.sessionUtil=sessionUtil;
        this.proxyUtil = proxyUtil;

    }


}
