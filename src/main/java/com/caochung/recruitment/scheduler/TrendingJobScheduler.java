package com.caochung.recruitment.scheduler;

import com.caochung.recruitment.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SCHEDULER-JOB-TRENDING")
public class TrendingJobScheduler {
    private final RedisTemplate<String, String> redisTemplate;
    @Scheduled(cron = "0 0 0 * * *")
    public void resetTrendingJobScheduler() {
        String key = "trending_jobs";
        Set<ZSetOperations.TypedTuple<String>> jobTrendings= redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
        if(jobTrendings!=null){
            for(ZSetOperations.TypedTuple<String> jobTrending:jobTrendings){
                String jobId = jobTrending.getValue();
                if(jobTrending.getScore()!=null){
                    double newScore=jobTrending.getScore()*0.5;
                    if(newScore<1.0){
                        redisTemplate.opsForZSet().remove(key,jobId);
                    }else {
                        redisTemplate.opsForZSet().add(key,jobId,newScore);
                    }
                }
            }
        }
        log.info("Reset trending job scheduler");
    }
}
