package edu.sysu;


import edu.sysu.cache.*;
import edu.sysu.filter.*;
import edu.sysu.monitor.CaseMonitor;
import edu.sysu.util.HibernateUtil;
import edu.sysu.util.ProxyUtil;
import edu.sysu.util.RequestForwarder;
import edu.sysu.util.SessionUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Created by gary on 16-7-27.
 */

@SpringBootApplication
public class YawlProxyApplication {
    public static void main(String[] args) {
        SpringApplication.run(YawlProxyApplication.class, args);

    }



    @Bean
    public Monitor monitor(){
        Monitor m= new Monitor(caseMonitor());
        m.start();
        return m;
    }

    @Bean
    public HibernateUtil hibernateUtil(){
        return new HibernateUtil();
    }

    @Bean
    public CaseCache caseCache(){return new CaseCache(hibernateUtil());}


    @Bean
    public EngineCache engineCache(){return new EngineCache(hibernateUtil());}

    @Bean
    public SpecificationCache specificationCache(){return new SpecificationCache(hibernateUtil());}

    @Bean
    public TenantCache tenantCache(){return new TenantCache(hibernateUtil());}

    @Bean
    public YawlServiceCache yawlServiceCache(){return new YawlServiceCache(hibernateUtil());}

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

        return new ProxyUtil(engineCache(),
                tenantCache(),
                requestForwarder(),
                sessionUtil(),
                caseCache(),
                specificationCache(),
                yawlServiceCache());

    }



    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new IBServlet(sessionUtil(),reverseProxy()),"/yawl/ib/*");
    }

    @Bean ServletRegistrationBean servletRegistrationBean2(){
        return new ServletRegistrationBean(new IAServlet(sessionUtil(),reverseProxy()),"/yawl/ia/*");
    }
    @Bean ServletRegistrationBean servletRegistrationBean3(){
        return new ServletRegistrationBean(new ResourceServiceServlet(reverseProxy(),requestForwarder()),"/resourceService/*");
    }


    @Bean
    CaseMonitor caseMonitor(){
        return new CaseMonitor(caseCache(),tenantCache());
    }

    //@Bean
    //ServletRegistrationBean servletRegistrationBean4(){
      //  return new ServletRegistrationBean(new MonitorServlet(caseMonitor()),"/myMonitor");

//    }







}
