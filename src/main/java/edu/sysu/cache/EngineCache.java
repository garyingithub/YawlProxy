package edu.sysu.cache;

import edu.sysu.data.Engine;
import edu.sysu.util.HibernateUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

/**
 * Created by gary on 16-8-8.
 */
public class EngineCache {
    private Map<String, Object> engines;

    private HibernateUtil hibernateUtil;

    public EngineCache(HibernateUtil hibernateUtil) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        engines = hibernateUtil.getObjectMap("Engine", "edu.sysu.data.Engine", "getEngineId");
        this.hibernateUtil=hibernateUtil;
    }

    public void storeEngine(Engine engine) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        hibernateUtil.storeObject(engine);
        this.engines.put(engine.getEngineId().toString(),engine);
    }

    public Engine getEngineById(String engineId)
    {
        return (Engine) this.getEngines().get(engineId);
    }

    private Map getEngines(){
        return this.engines;
    }

    Random random=new Random();
    public Engine getTargetEngine(){
        return (Engine) engines.values().toArray()[Math.abs(random.nextInt())%1];
    }
}
