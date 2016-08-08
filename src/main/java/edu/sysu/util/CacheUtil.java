package edu.sysu.util;

import edu.sysu.data.Engine;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.data.YawlService;
import org.slf4j.LoggerFactory;

import edu.sysu.data.Case;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by gary on 16-8-8.
 */
public class CacheUtil {



    public final String encryptedAdminPassword="Se4tMaQCi9gr0Q2usp7P56Sk5vM=";


    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    private HibernateUtil hibernateUtil;



    public CacheUtil() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        try {



        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // init for testing
        if (tenants.size() == 0) {
            Tenant tenant = new Tenant();
            tenant.setName("Peter");
            this.storeTenant(tenant);

            Tenant tenant1 = new Tenant();
            tenant1.setName("Gary");

            this.storeTenant(tenant1);


            Engine engine = new Engine();
            engine.setUrl("http://192.168.199.175:8086");
            this.storeEngine(engine);

            Engine engine1 = new Engine();
            engine1.setUrl("http://192.168.199.201:8080");
            this.storeEngine(engine1);

            YawlService yawlService = new YawlService();
            yawlService.setName("resourceService");
            yawlService.setUri("http://192.168.199.175:8086/resourceService/ib");
            yawlService.setDocument("resource");
            yawlService.setPassword("resource");
            yawlService.setTenant(tenant1);


            this.storeYawlService(yawlService);

        }
    }






































}

