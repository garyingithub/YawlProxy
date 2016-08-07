package edu.sysu.util;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

/**
 * Created by gary on 16-7-31.
 */
@Singleton
public class RequestForwarder extends Interface_Client {


    public String forwardRequest(String urlDestination, Map<String, String> parameterMap)
            throws IOException
    {

        String result;


        result = executePost(urlDestination, parameterMap);


        return result;
    }
}
