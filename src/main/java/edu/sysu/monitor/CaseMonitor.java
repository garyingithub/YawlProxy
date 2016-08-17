package edu.sysu.monitor;

import edu.sysu.cache.CaseCache;
import edu.sysu.cache.TenantCache;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import edu.sysu.data.Case;
/**
 * Created by gary on 16-8-12.
 */
public class CaseMonitor {

    private Map<String,Map<String,Integer>> tenantCaseCountingMap=new HashMap<>();
    private Map<String,Map<String,Integer>> tenantCaseSpeedMap=new HashMap<>();


    private Logger logger= LoggerFactory.getLogger(this.getClass());
    private CaseCache caseCache;
    private TenantCache tenantCache;
    public Map getTenantData(Tenant tenant){
        Map result=new HashMap<>();
      for(Specification specification:tenant.getSpecifications())  {
          for(Case c:specification.getCases()){
              result.put(c.getEngineIdAndInnerId(),tenantCaseSpeedMap.get(tenant.getTenantId())
                      .get(c.getEngineIdAndInnerId()));
          }
      }
        return result;
    }

    public void updateMap(Map<String,Integer> curMap){


        for(String k:curMap.keySet()){
            int value=curMap.get(k);
            logger.info(k);
            logger.info(String.valueOf(curMap.get(k)));

            Case c=caseCache.getCaseByEngineIdAndInnerId(k);
            Tenant t=c.getSpecification().getTenant();

            
            Map caseCountingMap=tenantCaseCountingMap.get(t.getTenantId().toString());

            if(!caseCountingMap.containsKey(k))
                caseCountingMap.put(k,0);
            caseCountingMap.put(k,(Integer)caseCountingMap.get(k)+value);
            tenantCaseSpeedMap.put(t.getTenantId().toString(),
                    curMap);
        }
    }

    public CaseMonitor(CaseCache caseCache,TenantCache tenantCache){
        this.caseCache=caseCache;
        this.tenantCache=tenantCache;

        for(Tenant t:tenantCache.getTenants()){
            this.tenantCaseSpeedMap.put(t.getTenantId().toString(),new HashMap<>());
            this.tenantCaseCountingMap.put(t.getTenantId().toString(),new HashMap<>());
        }


    }
}
