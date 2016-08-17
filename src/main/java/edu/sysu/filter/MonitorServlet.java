package edu.sysu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import edu.sysu.monitor.CaseMonitor;
import org.apache.logging.log4j.core.layout.LoggerFields;

import org.jdom2.JDOMException;
import org.w3c.dom.Document;

import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gary on 16-8-11.
 */
public class MonitorServlet extends HttpServlet {

    private static final Logger logger= LoggerFactory.getLogger(MonitorServlet.class);


    private CaseMonitor caseMonitor;
    public MonitorServlet(CaseMonitor caseMonitor){
        this.caseMonitor=caseMonitor;
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String[] folders = req.getRequestURI().split("/");
        String engine_id_string;
        if (folders.length<=3)
        {
            engine_id_string="0";
        }
        else
            engine_id_string = folders[3];

        logger.info(req.getParameter("countingMap"));
        String countingMapXML=req.getParameter("countingMap");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        InputStream inputStream=new ByteArrayInputStream(countingMapXML.getBytes());
        DocumentBuilder documentBuilder= null;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document= null;
        try {
            document = documentBuilder.parse(inputStream);
        } catch (SAXException e) {
            e.printStackTrace();
        }

        Map<String ,Integer> countingMap=new HashMap<>();
        Element element=document.getDocumentElement();
        NodeList nodeList=element.getElementsByTagName("case");
        for(int i=0;i<nodeList.getLength();i++){
            Element caseElement= (Element) nodeList.item(i);
            if(caseElement.getAttribute("value").isEmpty())
                continue;
            countingMap.put(engine_id_string+":"+caseElement.getAttribute("caseID"),
                    Integer.valueOf(caseElement.getAttribute("value")));
        }
        caseMonitor.updateMap(countingMap);

    }
}
