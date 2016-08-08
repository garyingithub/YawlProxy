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

    public YawlServiceCache(HibernateUtil hibernateUtil) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.hibernateUtil=hibernateUtil;
        yawlServices = hibernateUtil.getObjectMap("YawlService", "edu.sysu.data.YawlService", "getTenantIdAndName");

    }

    public void storeYawlService(YawlService yawlService) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        hibernateUtil.storeObject(yawlService);
        this.yawlServices.put(yawlService.getTenantIdAndName(),yawlService);
    }

    public YawlService getYawlServiceByTenantIdAndName(String tenantIdAndName){
        return (YawlService) this.yawlServices.get(tenantIdAndName);
    }

}
