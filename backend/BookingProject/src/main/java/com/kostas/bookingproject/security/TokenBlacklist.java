package com.kostas.bookingproject.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory blacklist of logged-out tokens.
 * Entries are dropped once older than the token lifetime (such tokens are expired anyway),
 * so the map cannot grow forever. NOTE: still per-instance and lost on restart;
 * use Redis / a Mongo TTL collection if you run more than one instance.
 */
@Component
public class TokenBlacklist {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public TokenBlacklist(@Value("${jwt.expiration-ms:86400000}") long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public void blacklist(String token) {
        purgeExpired();
        blacklist.put(token, System.currentTimeMillis());
    }

    public boolean isBlacklisted(String token) {
        Long addedAt = blacklist.get(token);
        if (addedAt == null) {
            return false;
        }
        if (System.currentTimeMillis() - addedAt > ttlMillis) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        blacklist.values().removeIf(addedAt -> now - addedAt > ttlMillis);
    }
}
