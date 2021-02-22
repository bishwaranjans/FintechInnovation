package io.bankbridge.seedwork;

import java.util.concurrent.TimeUnit;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import io.bankbridge.model.BankModelList;

public class CacheHelper {

    private CacheManager cacheManager;

    public final Cache<String, BankModelList> cacheDataList;

    public CacheHelper() {
        System.getProperties().setProperty("java -Dnet.sf.ehcache.use.classic.lru", "true");
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        cacheDataList = cacheManager.createCache("cacheOfBanksList",
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(String.class, BankModelList.class, ResourcePoolsBuilder.heap(10))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(60, TimeUnit.SECONDS))));
    }

    public void putInList(BankModelList banks) {
        cacheDataList.put(Constants.CACHE_BANKS, banks);
    }
}
