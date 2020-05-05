package com.oracle.coherence.demo.cachestore;

import com.tangosol.net.cache.CacheLoader;
import com.tangosol.util.Base;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An implementation of a JPA Cache Loader.
 *
 * @author Tim Middleton
 */
public class JpaCacheLoader extends Base implements CacheLoader {


    /**
     * {@link Map} of factories keyed on unit name.
     */
    protected static final Map mapFactories = new HashMap();


    /**
     * Entity name.
     */
    protected String entityName;


    /**
     * Entity class.
     */
    protected Class entityClass;


    /**
     * Entity manager factory.
     */
    protected EntityManagerFactory emf;


    /**
     * Construct a {@link JpaCacheLoader} with no {@link ClassLoader}.
     *
     * @param entityName       entity name
     * @param entityClassName  entity class
     * @param unitName         unit name
     */
    public JpaCacheLoader(String entityName, String entityClassName, String unitName)
    {
        initialize(entityName, entityClassName, unitName, null);
    }


    /**
     * Construct a {@link JpaCacheLoader} with a {@link ClassLoader}.
     *
     * @param entityName       entity name
     * @param entityClassName  entity class
     * @param unitName         unit name
     * @param loader           class loader
     */
    public JpaCacheLoader(String entityName, String entityClassName, String unitName, ClassLoader loader)
    {
        initialize(entityName, entityClassName, unitName, loader);
    }


    @Override
    public Object load(Object key)
    {
        EntityManager em = getEntityManager();

        Object value;
        try {
            value = em.find(entityClass, key);
        }
        finally
        {
            em.close();
        }

        return value;
    }


    @Override
    public Map loadAll(Collection keys)
    {
        EntityManager em = getEntityManager();

        try
        {
            Map      mapResult = new HashMap();
            Iterator iter      = keys.iterator();

            while (iter.hasNext())
            {
                Object key   = iter.next();
                Object value = em.find(entityClass, key);
                if (value != null)
                {
                    mapResult.put(key, value);
                }
            }

            return mapResult;
        }
        finally
        {
            em.close();
        }
    }


    /**
     * Initialize the {@link JpaCacheLoader}.
     *
     * @param entityName       entity name
     * @param entityClassName  entity class
     * @param unitName         unit name
     * @param loader           class loader
     */
    protected void initialize(String entityName, String entityClassName, String unitName, ClassLoader loader)
    {
        if (entityName == null | entityClassName == null | unitName == null)
        {
            throw new IllegalArgumentException(
                    "Entity name, fully-qualified entity class name, and persistence unit name must be specified");
        }
        else
        {
            this.entityName = entityName;
            if (loader == null)
            {
                loader = getContextClassLoader();
            }

            try
            {
                this.entityClass = loader.loadClass(entityClassName);
            }
            catch (ClassNotFoundException var8)
            {
                throw ensureRuntimeException(var8, "Class " + entityClassName + " could not be loaded");
            }

            synchronized (mapFactories)
            {
                emf = (EntityManagerFactory) mapFactories.get(unitName);
                if (emf == null)
                {
                    mapFactories.put(unitName, emf = Persistence.createEntityManagerFactory(unitName));
                }
            }
        }
    }


    /**
     * Return the {@link EntityManager}.
     * @return  the {@link EntityManager}
     */
    protected EntityManager getEntityManager()
    {
        return this.emf.createEntityManager();
    }
}
