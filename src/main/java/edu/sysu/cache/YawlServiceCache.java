package edu.sysu.cache;

import edu.sysu.data.YawlService;
import edu.sysu.util.HibernateUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by gary on 16-8-8.
 */
public class YawlServiceCache {
    private Map<String, Object> yawlServices;


    private HibernateUtil hibernateUtil;


    public YawlServiceCache(HibernateUtil hibernateUtil)   {
        this.hibernateUtil=hibernateUtil;
        try {
            yawlServices = hibernateUtil.getObjectMap("YawlService", "edu.sysu.data.YawlService", "getUri");
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void storeYawlService(YawlService yawlService) {
        hibernateUtil.storeObject(yawlService);
        this.yawlServices.put(yawlService.getUri(),yawlService);
    }

    public YawlService getYawlServiceByUri(String uri){
        return (YawlService) this.yawlServices.get(uri);
    }

}
