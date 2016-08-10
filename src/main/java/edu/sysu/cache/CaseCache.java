package edu.sysu.cache;

/**
 * Created by gary on 16-8-8.
 */
import edu.sysu.data.Case;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class CaseCache {

    private Map<String,Object> innerCasesMap;
    private Map<String,Object> outerCasesMap;

    private HibernateUtil hibernateUtil;
    public CaseCache(HibernateUtil hibernateUtil)   {
        this.hibernateUtil=hibernateUtil;

        try {
            innerCasesMap = hibernateUtil.getObjectMap("Case", "edu.sysu.data.Case", "getCaseId");
            outerCasesMap = hibernateUtil.getObjectMap("Case", "edu.sysu.data.Case", "getEngineIdAndInnerId");

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void storeCase(Case c) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        hibernateUtil.storeObject(c);
        this.innerCasesMap.put(c.getEngineIdAndInnerId(),c);
        this.outerCasesMap.put(c.getCaseId().toString(),c);
    }

    public Case getCaseByEngineIdAndInnerId(String EngineIdAndInnerId){
        Case c=  (Case) innerCasesMap.get(EngineIdAndInnerId);
        // in case that the announceCaseStart arrive before launchCase finish
        while (c==null){
            c= (Case) innerCasesMap.get(EngineIdAndInnerId);
        }

        return c;
    }

    public Tenant getTenantByCaseId(String caseId){
        Case c= this.getCaseByEngineIdAndInnerId(caseId);

        return c.getSpecification().getTenant();
    }

    public void deleteCase(Case c){
        c.getSpecification().getCases().remove(c);
        c.getEngine().getCases().remove(c);
        this.innerCasesMap.remove(c.getEngineIdAndInnerId());
        this.outerCasesMap.remove(c.getCaseId());
        hibernateUtil.deleteObject(c);
    }
    public edu.sysu.data.Case getCaseById(String caseId){
        return (Case) this.outerCasesMap.get(caseId);

    }


}
