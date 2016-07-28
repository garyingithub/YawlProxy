package edu.sysu;

import com.netflix.client.http.HttpRequest;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by gary on 16-7-28.
 */
public class ReverseProxyFilter extends ZuulFilter{

    private static Logger logger=LoggerFactory.getLogger(ReverseProxyFilter.class);
    @Override
    public String filterType() {
        return "router";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        RequestContext context=RequestContext.getCurrentContext();
        HttpServletRequest request=context.getRequest();

        logger.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;
    }
}
