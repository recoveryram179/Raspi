package com.hawks.raspi.services;

import com.hawks.raspi.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@EnableScheduling
@Component
public class AliveService {

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedDelay = 840_000) // 14 min
    public void keepAlive() {
        if (!Constants.amIWin) {
            try {
                String url = Constants.base_url + "alive"; // or any endpoint
                HashMap<String, Object> result = restTemplate.getForObject(url, HashMap.class);
                System.out.println(result.toString());
            } catch (Exception e) {
                System.out.println("Something: " + e.getMessage());
            }
        }
    }

}
