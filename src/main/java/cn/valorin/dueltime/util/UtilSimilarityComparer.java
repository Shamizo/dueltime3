package cn.valorin.dueltime.util;


import java.util.*;

public class UtilSimilarityComparer {
    /**
     * @param str1 待比较的字符串1
     * @param str2 待比较的字符串2
     * @return 字符串相似度（范围：0~1）
     */
    public static float getDegree(String str1, String str2) {
        int[][] d;
        int n = str1.length();
        int m = str2.length();
        int i;
        int j;
        char ch1;
        char ch2;
        int temp;
        if (n == 0 || m == 0) {
            return 0;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        for (i = 1; i <= n; i++) {
            ch1 = str1.charAt(i - 1);
            for (j = 1; j <= m; j++) {
                ch2 = str2.charAt(j - 1);
                if (ch1 == ch2 || ch1 + 32 == ch2 || ch2 + 32 == ch1) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                d[i][j] = Math.min(
                        Math.min(d[i - 1][j] + 1,
                                d[i][j - 1] + 1),
                        d[i - 1][j - 1] + temp);
            }
        }
        return (1 - (float) d[n][m] / Math.max(str1.length(), str2.length())) * 100F;
    }

    /**
     * @param enter     待比较的字符串
     * @param candidates 备选字符串集合
     * @param threshold 相似度阈值
     * @return 备选字符串集合中与待比较字符串相似度最高者。但若最高者小于相似度阈值，则待比较字符串会被视为不可进行相似度匹配，返回null
     */
    public static <T extends String> T getMostSimilar(String enter, Collection<T> candidates, double threshold) {
        return candidates.stream()
                .map(candidate -> new AbstractMap.SimpleEntry<>(candidate, getDegree(enter, candidate)))
                .filter(entry -> entry.getValue() > threshold)
                .max(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
                .map(AbstractMap.SimpleEntry::getKey)
                .orElse(null);
    }

        /**
         * 如果不输入相似度阈值，则使用默认值0.5
         */
    public static <T extends String> T getMostSimilar(String enter,Collection<T> candidates) {
        return getMostSimilar(enter, candidates, 0.5);
    }
}
