package edu.sysu;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import edu.sysu.data.Services.TenantService;
import edu.sysu.data.repositories.TenantRepository;
import edu.sysu.filter.IAServlet;
import edu.sysu.filter.IBServlet;

import edu.sysu.filter.ResourceServiceServlet;
import edu.sysu.util.*;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;


import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by gary on 16-7-27.
 */

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class YawlProxyApplication {
    public static void main(String[] args) {


        SpringApplication.run(YawlProxyApplication.class, args);


    }




    @Bean
    public HibernateUtil hibernateUtil(){
        return new HibernateUtil();
    }




    @Bean
    public RequestForwarder requestForwarder(){
        return new RequestForwarder();
    }

    @Bean
    public SessionUtil sessionUtil(){
        return new SessionUtil(requestForwarder(),hibernateUtil());
    }

    @Bean
    public ProxyUtil reverseProxy(){
        try {
            return new ProxyUtil(requestForwarder(),sessionUtil(),hibernateUtil());
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }



    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new IBServlet(sessionUtil(),reverseProxy()),"/yawl/ib/*");
    }

    @Bean ServletRegistrationBean servletRegistrationBean2(){
        return new ServletRegistrationBean(new IAServlet(sessionUtil(),reverseProxy()),"/yawl/ia/*");
    }
 //   @Bean ServletRegistrationBean servletRegistrationBean3(){
 //       return new ServletRegistrationBean(new ResourceServiceServlet(reverseProxy(),requestForwarder()),"/resourceService/*");
 //   }





    private static final String PROPERTY_NAME_DATABASE_DRIVER = "db.driver";
    private static final String PROPERTY_NAME_DATABASE_PASSWORD = "db.password";
    private static final String PROPERTY_NAME_DATABASE_URL = "db.url";
    private static final String PROPERTY_NAME_DATABASE_USERNAME = "db.username";

    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String PROPERTY_NAME_HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
    private static final String PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY = "hibernate.ejb.naming_strategy";
    private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";

    private static final String PROPERTY_NAME_MESSAGESOURCE_BASENAME = "message.source.basename";
    private static final String PROPERTY_NAME_MESSAGESOURCE_USE_CODE_AS_DEFAULT_MESSAGE = "message.source.use.code.as.default.message";

    @Resource
    private Environment environment;


    @Bean
    public DataSource dataSource() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();

        try {
            dataSource.setDriverClass(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        dataSource.setJdbcUrl(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_URL));
        dataSource.setUser(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_USERNAME));
        dataSource.setPassword(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_PASSWORD));

        return dataSource;
    }

    @Bean
    public JpaTransactionManager transactionManager() throws ClassNotFoundException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();


        transactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());

        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() throws ClassNotFoundException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan(environment.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
        entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        Properties jpaProterties = new Properties();
        jpaProterties.put(PROPERTY_NAME_HIBERNATE_DIALECT, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
        jpaProterties.put(PROPERTY_NAME_HIBERNATE_FORMAT_SQL, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_FORMAT_SQL));
        jpaProterties.put(PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY));
        jpaProterties.put(PROPERTY_NAME_HIBERNATE_SHOW_SQL, environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));

        entityManagerFactoryBean.setJpaProperties(jpaProterties);

        return entityManagerFactoryBean;
    }








}
