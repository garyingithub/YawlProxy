package edu.sysu.filter;



import edu.sysu.ReverseProxy;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;
import edu.sysu.util.SessionUtil;
import edu.sysu.util.YawlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import sun.util.calendar.BaseCalendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by gary on 16-7-29.
 */
@WebServlet(urlPatterns = "/yawl/ib/*")
public class IBServlet extends HttpServlet{

    Logger logger= LoggerFactory.getLogger(this.getClass());
    YawlUtil yawlUtil;
    SessionUtil sessionUtil;
    ReverseProxy reverseProxy;


    private OutputStreamWriter prepareResponse(HttpServletResponse response) throws IOException {
        response.setContentType("text/xml; charset=UTF-8");
        return new OutputStreamWriter(response.getOutputStream(), "UTF-8");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        logger.info(req.getRequestURI());
        logger.info(req.getMethod());
        logger.info(req.getParameter("action"));



        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info(req.getRequestURI());
        logger.info(req.getMethod());
        logger.info(req.getParameter("action"));

        OutputStreamWriter outputWriter = this.prepareResponse(resp);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(req));
        output.append("</response>");


        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();


    }



    private String processPostQuery(HttpServletRequest request) throws IOException {

        String[] folders = request.getRequestURI().split("/");
        String tenant_id_string = "";
        if (folders.length<=3)
        {
            tenant_id_string="2";
        }
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



        if(action!=null)
            switch (action){
                case "connect":
                    Tenant tenant= reverseProxy.getTenant(tenant_id_string);
                    msg.append(sessionUtil.connectToProxy(tenant,userId));

                    break;
                case "checkSession":
                    msg.append("<success/>");
                    break;
                case "disconnect":
                    msg.append("<success/>");
                    break;
                case "getSpecificationPrototypesList":
                    msg.append(reverseProxy.getSpecificationList());
                    break;
                case "launchCase":


                    break;
                case "startOne":
                    break;
                case "getLiveItems":
                    break;
                case "getAllRunningCases":
                    break;

            }else{
            if(request.getRequestURI().endsWith("ib")){
                msg.append(yawlUtil.successMessage(""));
            }
        }


        return msg.toString();
    }

    public IBServlet(YawlUtil yawlUtil,SessionUtil sessionUtil,ReverseProxy reverseProxy){
        this.yawlUtil=yawlUtil;
        this.sessionUtil=sessionUtil;
        this.reverseProxy=reverseProxy;
    }


}
