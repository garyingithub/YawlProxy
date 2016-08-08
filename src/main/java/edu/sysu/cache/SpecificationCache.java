package edu.sysu.cache;

import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by gary on 16-8-8.
 */
public class SpecificationCache {

    private Map<String,Object> specifications;

    private HibernateUtil hibernateUtil;

    public SpecificationCache(HibernateUtil hibernateUtil) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.hibernateUtil=hibernateUtil;
        specifications = hibernateUtil.getObjectMap("Specification", "edu.sysu.data.Specification", "getIdAndVersion");

    }
    public void storeSpecification(Specification specification) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        hibernateUtil.storeObject(specification);
        this.specifications.put(specification.getIdAndVersion(),specification);
    }
    public Specification getSpecificationByIdAndVersion(String idAndVersion){
        return (Specification) specifications.get(idAndVersion);
    }

    public Tenant getTenantBySpecificationNaturalId(String specificationNaturalId){
        Specification specification= (Specification) this.specifications.get(specificationNaturalId);
        return specification.getTenant();
    }
}
