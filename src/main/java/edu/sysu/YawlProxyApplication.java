package edu.sysu;

import edu.sysu.util.HibernateUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;



/**
 * Created by gary on 16-7-27.
 */
@EnableZuulProxy
@SpringBootApplication
public class YawlProxyApplication {
    public static void main(String[] args) {
//        HibernateUtil util=new HibernateUtil();
        SpringApplication.run(YawlProxyApplication.class, args);
    }

    @Bean
    public ReverseProxyFilter baseRouterFilter(){
        return new ReverseProxyFilter();
    }
}
