package org.groundwork.rs.resources;

import com.groundwork.collage.CollageEhCacheProvider;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoCacheState;
import org.groundwork.rs.dto.DtoCacheStateList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/cache")
public class CacheStatisticsResource {
    public static final String RESOURCE_PREFIX = "/cache/";
    protected static Log log = LogFactory.getLog(CacheStatisticsResource.class);

    private boolean calculateObjectCount = true;
    private Map<String, CalculatedState> calculatedStates = Collections.synchronizedMap(new HashMap<String, CalculatedState>());

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCacheStateList getCaches() {
        List<DtoCacheState> cacheStates = new ArrayList<>();
        try {
            CacheManager manager = CollageEhCacheProvider.getInstance();
            String[] cacheNames = manager.getCacheNames();
            for (String cacheName : cacheNames) {
                DtoCacheState cache = snapshotAndCalculateStatistics(manager, cacheName, true);
                cacheStates.add(cache);
            }
            return new DtoCacheStateList(cacheStates);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for cache state.").build());
        }
        finally {
        }
    }

    protected DtoCacheState snapshotAndCalculateStatistics(CacheManager cacheManager, String name, boolean calculate)
    {
        Cache cache = cacheManager.getCache(name);
        Ehcache ehCache = cacheManager.getEhcache(name);
        DtoCacheState state = new DtoCacheState(name);
        Statistics statistics = cache.getStatistics();
        state.setMemoryStoreSize(cache.getMemoryStoreSize());
        if (calculate)
        {
            state.setInMemorySize(cache.calculateInMemorySize());
            if (calculateObjectCount)
            {
                state.setObjectCount(statistics.getObjectCount());
            }
            else
            {
                state.setObjectCount(0);
            }
            calculatedStates.put(name, new CalculatedState(state.getInMemorySize(), state.getObjectCount()));
        }
        else
        {
            CalculatedState cs = calculatedStates.get(name);
            if (cs == null)
            {
                state.setInMemorySize(0);
                state.setObjectCount(0);
            }
            else
            {
                state.setInMemorySize(cs.inMemorySize);
                state.setObjectCount(cs.objectCount);
            }
        }
        state.setSize(cache.getSize());
        state.setDiskStoreSize(cache.getDiskStoreSize());
        // NOT AVAILABLE state.setAverageGetTime(statistics.getAverageGetTime());
        state.setCacheHits(statistics.getCacheHits());
        state.setCacheMisses(statistics.getCacheMisses());
        // NOT AVAILABLE state.setEvictionCount(statistics.getEvictionCount());
        // NOT AVAILABLE state.setInMemoryHits(statistics.getInMemoryHits());
        state.setOnDiskHits(statistics.getOnDiskHits());
        state.setMaxElementsInMemory(ehCache.getMaxElementsInMemory());
        state.setMaxElementsOnDisk(ehCache.getDiskStoreSize());
        state.setTimeToIdle(ehCache.getTimeToIdleSeconds());
        state.setTimeToLive(ehCache.getTimeToLiveSeconds());
        return state;
    }

    class CalculatedState {

        private long inMemorySize;
        private long objectCount;

        CalculatedState(long inMemorySize, long objectCount)
        {
            this.inMemorySize = inMemorySize;
            this.objectCount = objectCount;
        }
    }


}
