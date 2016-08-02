package edu.sysu.util;

import edu.sysu.ReverseProxy;
import edu.sysu.data.Engine;
import edu.sysu.data.Tenant;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */


public class SessionUtil {



    private Map<String,String> outerSessionMap=new HashMap<>();


    private Map<String,String> convertMap=new HashMap<>();


    /*
        session between proxy and the engines with admin account
        key: engineId
        value: sessionHandelh
     */

    private Map<String,String> adminSessionMap=new HashMap<>();


    /*
        session between proxy and the engines with defalutWorklist account
        key: engineId
        value: sessionHandle
     */

    private Map<String,String > defaultWorklistSessionMap=new HashMap<>();
    private YawlUtil yawlUtil;

    private HibernateUtil hibernateUtil;
    public void addEngine(Engine engine){
        try {
            adminSessionMap.put(engine.getEngineId().toString(),yawlUtil.connectToEngineAsAdmin(engine));
            adminSessionMap.put(engine.getEngineId().toString(),yawlUtil.connectToEngineAsDefaultWorklist(engine));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public String connectToProxy(Tenant tenant,String userId){

        if(!outerSessionMap.containsKey(tenant.getTenantId()+userId)){
            outerSessionMap.put(tenant.getTenantId()+userId, UUID.randomUUID().toString());
        }

        return outerSessionMap.get(tenant.getTenantId()+userId);
    }

    ReverseProxy reverseProxy;
    public SessionUtil(ReverseProxy reverseProxy,YawlUtil yawlUtil){
        this.reverseProxy=reverseProxy;
        this.yawlUtil=yawlUtil;
        Set<Map.Entry> engineEntries=reverseProxy.getEngines().entrySet();

        for(Map.Entry<String,Object> entry : engineEntries){
            this.addEngine((Engine) entry.getValue());
        }
    }







}
