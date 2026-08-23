package com.example.shorturl.repository;

import com.example.shorturl.entity.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

    List<ClickLog> findByShortUrlId(Long shortUrlId);

    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM ClickLog c WHERE c.shortUrlId = :shortUrlId")
    long countDistinctIpByShortUrlId(@Param("shortUrlId") Long shortUrlId);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickLog c WHERE c.shortUrlId = :shortUrlId GROUP BY c.deviceType")
    List<Object[]> countClicksByDevice(@Param("shortUrlId") Long shortUrlId);

    @Query("SELECT c.browser, COUNT(c) FROM ClickLog c WHERE c.shortUrlId = :shortUrlId GROUP BY c.browser")
    List<Object[]> countClicksByBrowser(@Param("shortUrlId") Long shortUrlId);

    @Query("SELECT c.os, COUNT(c) FROM ClickLog c WHERE c.shortUrlId = :shortUrlId GROUP BY c.os")
    List<Object[]> countClicksByOs(@Param("shortUrlId") Long shortUrlId);

    @Query("SELECT c.referer, COUNT(c) FROM ClickLog c WHERE c.shortUrlId = :shortUrlId GROUP BY c.referer")
    List<Object[]> countClicksByReferer(@Param("shortUrlId") Long shortUrlId);

    @Query(value = "SELECT DATE_FORMAT(clicked_at, '%Y-%m-%d') as clickDate, COUNT(*) as count " +
            "FROM click_logs WHERE short_url_id = :shortUrlId " +
            "GROUP BY DATE_FORMAT(clicked_at, '%Y-%m-%d') ORDER BY clickDate ASC", nativeQuery = true)
    List<Object[]> countClicksByDate(@Param("shortUrlId") Long shortUrlId);
}