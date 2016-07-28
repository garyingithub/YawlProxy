import edu.sysu.data.Case;
import edu.sysu.data.Engine;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import edu.sysu.util.HibernateUtil;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gary on 16-7-27.
 */
public class DataCache {

    private static Map<String,Tenant> tenantMap=new HashMap<>();
    private static Map<String,Specification> specificationMap=new HashMap<>();
    private static Map<String,Case> caseMap=new HashMap<>();
    private static Map<String,Engine> engineMap=new HashMap<>();

    private static Map loadDataToMap(String tableName, String className,String idFieldName){
        List objects= HibernateUtil.getAllObjects(tableName);
        Map<String,Object> objectMap=new HashMap<>();

        try {
            for(int i=0;i<objects.size();i++){
                Field field=Class.forName(className).getField(idFieldName);
                String id=field.get(objects.get(i)).toString();
                objectMap.put(id,Class.forName(className).cast(objects.get(i)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return objectMap;
        }

    }


/*
    public static void main(String[] args){

        Tenant tenant=new Tenant();
        tenant.setName("Peter");

        Specification specification=new Specification();

        specification.setIdAndVersion("1023:v3");
        specification.setXML("<>");


        tenant.addSpecification(specification);
        Engine engine=new Engine();
        engine.setUrl("gg.com");
        engine.addSpecification(specification);

        Case c=new Case();
        c.setInnerId("11:2");
        c.setOuterId("123:333");
        c.setSpecification(specification);

        engine.addCase(c);


    }
*/

}
