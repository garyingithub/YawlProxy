package edu.sysu.cache;

import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by gary on 16-8-8.
 */
public class TenantCache {
    private Map<String,Object> tenants;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private HibernateUtil hibernateUtil;

    public TenantCache(HibernateUtil hibernateUtil) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.hibernateUtil=hibernateUtil;
        tenants = hibernateUtil.getObjectMap("Tenant", "edu.sysu.data.Tenant", "getTenantId");
    }

    public void storeTenant(Tenant tenant){
        hibernateUtil.storeObject(tenant);
        this.tenants.put(tenant.getTenantId().toString(),tenant);
    }

    public Tenant getTenantById(String tenantId){
        return (Tenant) this.tenants.get(tenantId);
    }



}
