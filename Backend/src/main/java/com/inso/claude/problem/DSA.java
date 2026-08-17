package com.inso.claude.problem;

import java.util.List;

public class DSA {

    public int longestStreak(List<Long> timestamps) {
        int currentStreak = 1;
        int longestStreak = 1;

        for (int i =1; i<timestamps.size(); i++){
            if(timestamps.get(i)-timestamps.get(i-1)<=86400){
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            }else {
                currentStreak=0;
            }
            }

        return longestStreak;
        }
}
