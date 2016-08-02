package edu.sysu;


import edu.sysu.filter.IAServlet;
import edu.sysu.filter.IBServlet;

import edu.sysu.util.HibernateUtil;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.SessionUtil;
import edu.sysu.util.YawlUtil;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.yawlfoundation.yawl.engine.interfce.EngineGateway;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;

import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.Naming;


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
    public ReverseProxy reverseProxy(){
        return new ReverseProxy(hibernateUtil(),yawlUtil());
    }

    @Bean
    public RequestForwarder requestForwarder(){
        return new RequestForwarder();
    }

    @Bean
    public SessionUtil sessionUtil(){
        return new SessionUtil(reverseProxy(),yawlUtil());
    }


    @Bean
    public YawlUtil yawlUtil(){
        return new YawlUtil(hibernateUtil());
    }


    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new IBServlet(yawlUtil(),sessionUtil(),reverseProxy()),"/yawl/ib/*");
    }

    @Bean ServletRegistrationBean servletRegistrationBean2(){
        return new ServletRegistrationBean(new IAServlet(yawlUtil(),sessionUtil(),reverseProxy(),requestForwarder()),"/yawl/ia/*");
    }





}
