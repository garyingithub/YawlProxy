package edu.sysu.util;





import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by gary on 16-7-27.
 */

public class HibernateUtil {

    private SessionFactory sessionFactory;

    private Logger logger=LoggerFactory.getLogger(HibernateUtil.class);

    private final StandardServiceRegistry registry;

    public HibernateUtil(){


        registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        logger.debug("succeed initializing hibernate");

        try {
            sessionFactory=new MetadataSources(registry).buildMetadata().buildSessionFactory();

        }catch (Exception e){
            StandardServiceRegistryBuilder.destroy(registry);
            logger.debug("fail to initialize hibernate,the cause is {}.",e.toString());
        }

    }

    public  void storeObject(Object object){
        Session session=sessionFactory.openSession();
        session.beginTransaction();
        session.save(object);
        session.getTransaction().commit();
        session.close();
        logger.debug("successfully store one object");
    }



    public List getAllObjects(String tableName){

        Session session=sessionFactory.openSession();

        List result=session.createQuery("from "+tableName).getResultList();

        session.close();
        logger.debug("get {}. lines of data from {}.",result.size(),tableName);
        return result;
    }

    public Map<String,Object> getObjectMap(String tableName,String className,String idName){

        List allObjects=getAllObjects(tableName);

        Map<String,Object> result=new HashMap<>();

        for(int i=0;i<allObjects.size();i++){
            try {
                String id= String.valueOf(Class.forName(className).getDeclaredMethod(idName).invoke(allObjects.get(i)));
                result.put(id,allObjects.get(i));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    public void updateObject(Object object){
        Session session=sessionFactory.openSession();
        session.beginTransaction();
        session.update(object);
        session.getTransaction().commit();
        session.close();
        logger.debug("successfully update one object");
    }






}
