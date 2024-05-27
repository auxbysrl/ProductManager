package com.auxby.productmanager.config.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CacheUtilsTest {

    @InjectMocks
    private CacheUtils cacheUtils;

    @Test
    void should_AddOfferIdsToCache_When_MethodIsCalled() {
        //Act
        cacheUtils.addOfferIds(List.of(10, 20));

        //Assert
        var result = cacheUtils.getCachedOfferIds();
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(10, 20)));
    }

    @Test
    void should_ReturnDummyListOfIds_When_NoCachedIdsSet() {
        //Act
        var result = cacheUtils.getCachedOfferIds();

        //Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(-1));
    }

    @Test
    void should_ReturnCacheIdsList_When_IdsSetBasedOnCounterValue() {
        //First Act
        var firstResult = cacheUtils.getCachedOfferIds();

        //Assert
        assertEquals(1, firstResult.size());
        assertTrue(firstResult.contains(-1));

        //Second Act
        cacheUtils.addOfferIds(List.of(10, 20, 30));
        var secondResult = cacheUtils.getCachedOfferIds();

        //Assert
        assertEquals(3, secondResult.size());
        assertTrue(secondResult.containsAll(List.of(10, 20, 30)));

        //Third Act
        cacheUtils.addOfferIds(List.of(100, 200, 300));
        var thirdResult = cacheUtils.getCachedOfferIds();

        //Assert
        assertEquals(3, secondResult.size());
        assertTrue(thirdResult.containsAll(List.of(100, 200, 300)));
    }
}