package edu.sysu.cache;

import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gary on 16-8-8.
 */
public class TenantCache {
    private Map<String,Object> tenants;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private HibernateUtil hibernateUtil;

    public List<Tenant> getTenants(){
        List<Tenant> result=new ArrayList<>();
        for(Object o:tenants.values()){
            result.add((Tenant) o);
        }
        return result;
    }

    public TenantCache(HibernateUtil hibernateUtil) {
        this.hibernateUtil=hibernateUtil;
        try {
            tenants = hibernateUtil.getObjectMap("Tenant", "edu.sysu.data.Tenant", "getTenantId");
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void storeTenant(Tenant tenant){
        hibernateUtil.storeObject(tenant);
        this.tenants.put(tenant.getTenantId().toString(),tenant);
    }

    public Tenant getTenantById(String tenantId){
        return (Tenant) this.tenants.get(tenantId);
    }

    public int getSize(){return tenants.size();}


}
