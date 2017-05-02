package com.taobao.rigel.rap.common.utils;
import com.taobao.rigel.rap.project.bo.Action;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by Bosn on 14/11/28.
 * Basic cache, need weight for string length.
 */
public class CacheUtils {
    private static final int DEFAULT_CACHE_EXPIRE_SECS = 600;
    private static final Logger logger = LogManager.getLogger(CacheUtils.class);

    public static final String KEY_MOCK_RULE = "KEY_MOCK_RULE:";
    public static final String KEY_MOCK_DATA = "KEY_MOCK_DATA";
    public static final String KEY_PROJECT_LIST = "KEY_PROJECT_LIST";
    public static final String KEY_CORP_LIST = "KEY_CORP_LIST";
    public static final String KEY_CORP_LIST_TOP_ITEMS = "KEY_CORP_LIST_TOP_ITEMS";
    public static final String KEY_WORKSPACE = "KEY_WORKSPACE";

    public static final String KEY_ACCESS_USER_TO_PROJECT = "KEY_ACCESS_USER_TO_PROJECT";
    public static final String KEY_NOTIFICATION = "KEY_NOTIFICATION";
    public static final String KEY_STATISTICS = "KEY_STATISTICS";
    public static final String KEY_STATISTICS_OF_TEAM = "KEY_STATISTICS_OF_TEAM";

    private static CacheManager cacheManager = ((CacheManager)SpringContextHolder.getBean("cacheManager"));
    private static final String SYS_CACHE = "sysCache";
    public CacheUtils() {}


    /**
     * get cached Mock rule
     *
     * @param action
     * @param pattern
     * @return
     */
    public static String getRuleCache(Action action, String pattern, boolean isMockData) {
        int actionId = action.getId();
        String requestUrl = action.getRequestUrl();
        if (requestUrl == null) {
            requestUrl = "";
        }
        if (pattern.contains("noCache=true") || requestUrl.contains("{")
                || requestUrl.contains("noCache=true")) {
            return null;
        }
        String [] cacheKey = new String[]{isMockData ? KEY_MOCK_DATA
                 : KEY_MOCK_RULE, new Integer(actionId).toString()};
        return get(cacheKey);
    }

    /**
     * set Mock rule cache
     *
     * @param actionId
     * @param result
     */
    public static void setRuleCache(int actionId, String result, boolean isMockData) {
        String[] cacheKey = new String[]{isMockData ? KEY_MOCK_DATA : KEY_MOCK_RULE, new Integer(actionId).toString()};
        put(cacheKey, result);
    }

    public static void removeCacheByActionId(int id) {
        String[] cacheKey1 = new String[]{KEY_MOCK_RULE, new Integer(id).toString()};
        String[] cacheKey2 = new String[]{KEY_MOCK_DATA, new Integer(id).toString()};

        remove(StringUtils.join(cacheKey1, "|"));
        remove(StringUtils.join(cacheKey2, "|"));

    }

    public static void put(String [] keys, String value, int expireInSecs) {
        String cacheKey = StringUtils.join(keys, "|");
        put(cacheKey, value);
//        if (expireInSecs > 0)
//            jedis.expire(cacheKey, expireInSecs);
    }

    public static void put(String [] keys, String value) {
        put(keys, value, DEFAULT_CACHE_EXPIRE_SECS);
    }

    public static String get(String []keys) {

        String cache = get(StringUtils.join(keys, "|")).toString();
        return cache;
    }

    public static void del(String[] keys) {
        String cacheKey = StringUtils.join(keys, "|");
        remove(cacheKey);
    }

    /**
     * 获取SYS_CACHE缓存
     * @param key
     * @return
     */
    public static Object get(String key) {
        return get(SYS_CACHE, key);
    }

    /**
     * 写入SYS_CACHE缓存
     * @param key
     * @return
     */
    public static void put(String key, Object value) {
        put(SYS_CACHE, key, value);
    }

    /**
     * 从SYS_CACHE缓存中移除
     * @param key
     * @return
     */
    public static void remove(String key) {
        remove(SYS_CACHE, key);
    }


    /**
     * 获取缓存
     * @param cacheName
     * @param key
     * @return
     */
    public static Object get(String cacheName, String key) {
        Element element = getCache(cacheName).get(key);
        return element==null?null:element.getObjectValue();
    }

    /**
     * 写入缓存
     * @param cacheName
     * @param key
     * @param value
     */
    public static void put(String cacheName, String key, Object value) {
        Element element = new Element(key, value);
        getCache(cacheName).put(element);
    }

    /**
     * 从缓存中移除
     * @param cacheName
     * @param key
     */
    public static void remove(String cacheName, String key) {
        getCache(cacheName).remove(key);
    }

    /**
     * 获得一个Cache，没有则创建一个。
     * @param cacheName
     * @return
     */
    private static Cache getCache(String cacheName){
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null){
            cacheManager.addCache(cacheName);
            cache = cacheManager.getCache(cacheName);
            cache.getCacheConfiguration().setEternal(true);
        }
        return cache;
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }

    public static void init() {

    }
}
