package edu.sysu.util;


import edu.sysu.data.Tenant;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gary on 16-7-27.
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private static Logger logger=LoggerFactory.getLogger(HibernateUtil.class);

    static {

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();

        try {
            sessionFactory=new MetadataSources(registry).buildMetadata().buildSessionFactory();

            logger.debug("succeed initializing hibernate");

        }catch (Exception e){
            StandardServiceRegistryBuilder.destroy(registry);
            logger.debug("fail to initialize hibernate,the cause is {}.",e.toString());
        }

    }

    public static void storeObject(Object object){
        Session session=sessionFactory.openSession();
        session.beginTransaction();
        session.save(object);
        session.getTransaction().commit();
        session.close();
        logger.debug("successfully store one object");
    }

    public static List getAllObjects(String tableName){

        Session session=sessionFactory.openSession();

        List result=session.createQuery("from "+tableName).getResultList();

        session.close();
        logger.debug("get {}. lines of data from {}.",result.size(),tableName);
        return result;
    }

    public static void updateObject(Object object){
        Session session=sessionFactory.openSession();
        session.beginTransaction();
        session.update(object);
        session.getTransaction().commit();
        session.close();
        logger.debug("successfully update one object");
    }



}
