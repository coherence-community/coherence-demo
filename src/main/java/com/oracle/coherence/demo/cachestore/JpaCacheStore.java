/*
 * File: JpaCacheStore.java
 *
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * You may not use this file except in compliance with the Universal Permissive
 * License (UPL), Version 1.0 (the "License.")
 *
 * You may obtain a copy of the License at https://opensource.org/licenses/UPL.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.oracle.coherence.demo.cachestore;

import com.tangosol.net.cache.CacheStore;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collection;
import java.util.Map;

/**
 * An implementation of a JPA Cache Store.
 *
 * @author Tim Middleton
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class JpaCacheStore extends JpaCacheLoader implements CacheStore {
    /**
     * Construct a {@link JpaCacheStore} with no {@link ClassLoader}.
     *
     * @param entityName       entity name
     * @param entityClassName  entity class
     * @param unitName         unit name
     */
    @SuppressWarnings("unused")
    public JpaCacheStore(String entityName, String entityClassName, String unitName)
    {
        super(entityName, entityClassName, unitName);
    }

    /**
     * Construct a {@link JpaCacheStore} with a {@link ClassLoader}.
     *
     * @param entityName       entity name
     * @param entityClassName  entity class
     * @param unitName         unit name
     * @param loader           class loader
     */
    @SuppressWarnings("unused")
    public JpaCacheStore(String entityName, String entityClassName, String unitName, ClassLoader loader)
    {
        super(entityName, entityClassName, unitName, loader);
    }


    @Override
    public void store(Object key, Object value)
    {
        EntityManager     em = this.getEntityManager();
        EntityTransaction tx = null;

        try
        {
            tx = em.getTransaction();
            tx.begin();
            em.merge(value);
            tx.commit();
        }
        catch (RuntimeException e)
        {
            this.rollback(tx);
            throw e;
        }
        finally
        {
            em.close();
        }
    }


    @Override
    public void storeAll(Map map)
    {
        EntityManager     em = this.getEntityManager();
        EntityTransaction tx = null;

        try
        {
            tx = em.getTransaction();
            tx.begin();

            for (Object value : map.values())
            {
                em.merge(value);
            }

            tx.commit();
        }
        catch (RuntimeException e)
        {
            this.rollback(tx);
            throw e;
        }
        finally
        {
            em.close();
        }
    }


    @Override
    public void erase(Object key)
    {
        EntityManager     em = this.getEntityManager();
        EntityTransaction tx = null;

        try
        {
            tx = em.getTransaction();
            tx.begin();
            Object value = em.find(entityClass, key);
            if (value != null)
            {
                em.remove(value);
            }

            tx.commit();
        }
        catch (RuntimeException e)
        {
            this.rollback(tx);
            throw e;
        }
        finally
        {
            em.close();
        }
    }


    @Override
    public void eraseAll(Collection keys)
    {
       EntityManager      em = this.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            for (Object key : keys)
            {
                Object value = em.find(entityClass, key);
                if (value != null)
                {
                    em.remove(value);
                }
            }

            tx.commit();
        }
        catch (RuntimeException e)
        {
            this.rollback(tx);
            throw e;
        }
        finally
        {
            em.close();
        }
    }

    protected void rollback(EntityTransaction tx)
    {
        try
        {
            if (tx != null && tx.isActive())
            {
                tx.rollback();
            }
        }
        catch (RuntimeException ignored)
        {
        }
    }
}
