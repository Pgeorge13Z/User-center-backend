package com.george.usercenter.constant;

public interface RedisKeyName {
    /**
     * 缓存预热锁
     */
    String PRECACHEJOB_DOCACHE_LOCK ="palLink:preCacheJob:doCache:lock";

    /**
     * 用户推荐缓存
     */
    String USER_RECOMMEND = "PalLink:user:recommend";

    /**
     * 用户加入队伍锁
     */
    String TEAM_JOIN_LOCK = "PalLink:join_team:lock";
}
