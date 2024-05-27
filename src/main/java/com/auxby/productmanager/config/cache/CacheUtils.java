package com.auxby.productmanager.config.cache;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheUtils {

    private final Set<Integer> cachedOfferIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addOfferIds(List<Integer> offerIds) {
        if (!cachedOfferIds.isEmpty()) {
            clearOfferIds();
        }
        cachedOfferIds.addAll(offerIds);
    }

    public Set<Integer> getCachedOfferIds() {
        if (cachedOfferIds.isEmpty()) {
            return Set.of(-1);
        }
        return new HashSet<>(Collections.unmodifiableSet(cachedOfferIds));
    }

    private void clearOfferIds() {
        cachedOfferIds.clear();
    }
}
