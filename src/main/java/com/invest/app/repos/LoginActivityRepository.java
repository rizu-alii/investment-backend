package com.invest.app.repos;

import com.invest.app.entities.LoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginActivityRepository extends JpaRepository<LoginActivity, Long> {

    @Query("SELECT COUNT(l) FROM LoginActivity l WHERE MONTH(l.loginTime) = MONTH(CURRENT_DATE) AND YEAR(l.loginTime) = YEAR(CURRENT_DATE)")
    long countLoginsThisMonth();

    long countByLoginTimeAfter(LocalDateTime dateTime);

    long countByLoginTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByMobileTrue();

    long countByMobileFalse();







    @Query("SELECT l.countryCode, COUNT(l) FROM LoginActivity l GROUP BY l.countryCode")
    List<Object[]> countLoginsByCountry();

    // Get top 5 latest login entries from unique users
    @Query("""
        SELECT l FROM LoginActivity l
        WHERE l.loginTime IN (
            SELECT MAX(sub.loginTime) FROM LoginActivity sub GROUP BY sub.username
        )
        ORDER BY l.loginTime DESC
        """)
    List<LoginActivity> findTop5LatestLoginsByUniqueUsers();









    List<LoginActivity> findTop5ByOrderByLoginTimeDesc();



}