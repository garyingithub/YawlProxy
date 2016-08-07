package edu.sysu.util;

import edu.sysu.data.Engine;
import edu.sysu.data.Tenant;
import edu.sysu.data.YawlService;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */


public class SessionUtil {

    private Map<String,String> outerSessionMap=new HashMap<>();

    private org.slf4j.Logger logger=LoggerFactory.getLogger(this.getClass());

    /*
        session between proxy and the engines with admin account
        key: engineId
        value: sessionHandelh
     */
    private Map<String,String> adminSessionMap=new HashMap<>();

    private RequestForwarder requestForwarder;

    public SessionUtil(RequestForwarder requestForwarder, HibernateUtil hibernateUtil) {

        HibernateUtil hibernateUtil1 = hibernateUtil;
        this.requestForwarder=requestForwarder;

        List engineEntries=hibernateUtil.getAllObjects("Engine");

        try {
            for(Object object : engineEntries) {
                this.addEngine((Engine) object);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addEngine(Engine engine) throws IOException {
        logger.debug(String.format("try to add an Engine ,the url is %s",engine.getUrl()));
        adminSessionMap.put(engine.getEngineId().toString(),connectToEngineAsAdmin(engine));
    }

    public String connectToProxy(Tenant tenant,String userId){

        logger.debug(String.format("Tenant %s 's user %s is connecting to the proxy",tenant.getTenantId(),userId ));
        if(!outerSessionMap.containsKey(tenant.getTenantId()+userId)){
            outerSessionMap.put(tenant.getTenantId()+userId, UUID.randomUUID().toString());
        }

        return outerSessionMap.get(tenant.getTenantId()+userId);
    }


    private String connectToEngine(Engine engine, String userId, String password) throws IOException {

        String destination = engine.getIBUri();

        RequestForwarder fw = new RequestForwarder();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "connect");
        parameters.put("userid", userId);
        parameters.put("password", password);



        String result = fw.forwardRequest(destination, parameters);

        if (result.startsWith("<")) {
            return result.substring(10, 10 + 36);
        } else {
            return result;
        }


    }

    private String connectToEngineAsAdmin(Engine engine) throws IOException {
        return connectToEngine(engine, "admin", "Se4tMaQCi9gr0Q2usp7P56Sk5vM=");
    }

    public String connectToEngineAsService(Engine engine, YawlService service) throws IOException {
        return connectToEngine(engine,service.getName(),service.getPassword());
    }



    private boolean checkAdminSession(Engine engine,String sessionHandle) throws IOException {

        Map<String,String> params=new HashMap<>();

        params.put("action","checkConnection");
        params.put("sessionHandle",sessionHandle);

        String result=requestForwarder.forwardRequest(engine.getIBUri(),params);

        return result.contains("success");

    }



    public String getAdminSession(Engine engine) throws IOException {

        String sessionHandle=this.adminSessionMap.get(engine.getEngineId().toString());
        if(checkAdminSession(engine,sessionHandle)){
            return sessionHandle;
        }else{
            int times=3;
            while (0<times){
                String temp=connectToEngineAsAdmin(engine);
                if(checkAdminSession(engine,temp))
                    return temp;
                times--;
            }
            throw new IOException("can't build session with Engine");
        }

    }





}
