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
public class IAServlet extends HttpServlet{

    Logger logger= LoggerFactory.getLogger(this.getClass());

    private OutputStreamWriter prepareResponse(HttpServletResponse response) throws IOException {
        response.setContentType("text/xml; charset=UTF-8");
        return new OutputStreamWriter(response.getOutputStream(), "UTF-8");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        logger.info(req.getRequestURI());
        logger.info(req.getMethod());
        logger.info(req.getQueryString());



        doPost(req,resp);
    }

    private RequestForwarder requestForwarder;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info(req.getRequestURI());
        logger.info(req.getMethod());
        logger.info(req.getQueryString());


        OutputStreamWriter outputWriter = this.prepareResponse(resp);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        try {
            output.append(processPostQuery(req));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        output.append("</response>");


        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();


    }

    SessionUtil sessionUtil;
    ProxyUtil proxyUtil;
    YawlUtil yawlUtil;
    private String processPostQuery(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {
        String[] folders = request.getRequestURI().split("/");
        String tenant_id_string="";
        if (folders.length<=3)
        {
            tenant_id_string="2";
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
            tenant= proxyUtil.getTenant(tenant_id_string);
        }
        if(action!=null)
        switch (action){
            case "connect":

                msg.append(sessionUtil.connectToProxy(tenant,userID));
                break;
            case "checkSession":
                msg.append("<success/>");
                break;
            case "disconnect":
                msg.append("<success/>");
                break;
            case "upload":
                String specXML=request.getParameter("specXML");

                String result= proxyUtil.loadSpecification(specXML,tenant);
                if(result.isEmpty()){
                    msg.append(yawlUtil.successMessage(""));
                }
                else {
                    msg.append(yawlUtil.failureMessage(result));
                }
                break;
            case "getList":
                break;
            case "getYAWLServices":
                break;
            case "getPassword":
                if(userID.equals("admin"))
                    msg.append(proxyUtil.encryptedAdminPassword);
                else if(userID.equals("editor"))
                    msg.append("VfrZ/SW35S1ytFXq9Giw7+A05wA=");
                break;
            case "getBuildProperties":
                msg.append(yawlUtil.getBuildProperties());
                break;

        }

        return msg.toString();
    }

    public IAServlet(YawlUtil yawlUtil, SessionUtil sessionUtil, ProxyUtil proxyUtil, RequestForwarder requestForwarder){
        this.yawlUtil=yawlUtil;
        this.sessionUtil=sessionUtil;
        this.proxyUtil = proxyUtil;
        this.requestForwarder=requestForwarder;
    }


}
