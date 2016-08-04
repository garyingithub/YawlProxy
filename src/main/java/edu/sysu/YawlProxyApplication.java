package edu.sysu;


import edu.sysu.filter.IAServlet;
import edu.sysu.filter.IBServlet;

import edu.sysu.filter.ResourceServiceServlet;
import edu.sysu.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.InvocationTargetException;


/**
 * Created by gary on 16-7-27.
 */

@SpringBootApplication
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
    @Bean ServletRegistrationBean servletRegistrationBean3(){
        return new ServletRegistrationBean(new ResourceServiceServlet(reverseProxy(),requestForwarder()));
    }





}
