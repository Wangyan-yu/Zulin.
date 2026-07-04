package com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图配置类
 * 用于读取和管理高德地图相关的配置信息
 */
@Configuration
public class AmapConfig {
    
    /**
     * 高德地图API密钥
     */
    @Value("${amap.key}")
    private String key;
    
    /**
     * 高德地图服务基础URL
     */
    @Value("${amap.base-url}")
    private String baseUrl;
    
    /**
     * 逆地理编码接口路径
     */
    @Value("${amap.regeo-path}")
    private String regeoPath;
    
    /**
     * 地理编码接口路径
     */
    @Value("${amap.geo-path}")
    private String geoPath;
    
    /**
     * 距离计算接口路径
     */
    @Value("${amap.distance-path}")
    private String distancePath;
    
    // Getter方法
    public String getKey() {
        return key;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getRegeoPath() {
        return regeoPath;
    }
    
    public String getGeoPath() {
        return geoPath;
    }
    
    public String getDistancePath() {
        return distancePath;
    }
    
    /**
     * 获取完整的逆地理编码API URL
     * @return 完整的API URL
     */
    public String getRegeoApiUrl() {
        return baseUrl + regeoPath;
    }
    
    /**
     * 获取完整的地理编码API URL
     * @return 完整的API URL
     */
    public String getGeoApiUrl() {
        return baseUrl + geoPath;
    }
    
    /**
     * 获取完整的距离计算API URL
     * @return 完整的API URL
     */
    public String getDistanceApiUrl() {
        return baseUrl + distancePath;
    }
}