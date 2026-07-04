package com.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.config.AmapConfig;

/**
* 高德地图工具类
*/
@Component
public class AmapUtil {
    
    private static AmapConfig amapConfig;
    
    @Autowired
    public void setAmapConfig(AmapConfig config) {
        AmapUtil.amapConfig = config;
    }
    
    /**
     * 根据经纬度获得省市区信息（逆地理编码）
     * @param lng 经度
     * @param lat 纬度
     * @return 包含省市区等信息的Map
     */
    public static Map<String, String> getCityByLonLat(String lng, String lat) {
        if (amapConfig == null) {
            return null;
        }
        
        String key = amapConfig.getKey();
        String location = lng + "," + lat;
        try {
            // 拼装url，使用高德地图逆地理编码API
            String url = amapConfig.getRegeoApiUrl() + "?key=" + key + "&location=" + location;
            String result = HttpClientUtils.doGet(url);
            JSONObject o = new JSONObject(result);
            
            // 检查返回状态
            String status = o.getString("status");
            if ("1".equals(status)) {
                Map<String, String> area = new HashMap<>();
                JSONObject regeocode = o.getJSONObject("regeocode");
                if (regeocode != null && !regeocode.isNull("addressComponent")) {
                    JSONObject addressComponent = regeocode.getJSONObject("addressComponent");
                    area.put("province", addressComponent.optString("province", ""));
                    area.put("city", addressComponent.optString("city", ""));
                    area.put("district", addressComponent.optString("district", ""));
                    area.put("street", addressComponent.optString("township", ""));
                    area.put("address", regeocode.optString("formatted_address", ""));
                }
                return area;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据地址获取经纬度（地理编码）
     * @param address 地址
     * @return 包含经纬度信息的Map
     */
    public static Map<String, String> getLonLatByAddress(String address) {
        if (amapConfig == null) {
            return null;
        }
        
        String key = amapConfig.getKey();
        try {
            // 对地址进行URL编码
            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
            // 拼装url，使用高德地图地理编码API
            String url = amapConfig.getGeoApiUrl() + "?key=" + key + "&address=" + encodedAddress;
            String result = HttpClientUtils.doGet(url);
            JSONObject o = new JSONObject(result);
            
            // 检查返回状态
            String status = o.getString("status");
            if ("1".equals(status) && o.getInt("count") > 0) {
                Map<String, String> location = new HashMap<>();
                JSONObject geocode = o.getJSONArray("geocodes").getJSONObject(0);
                String locationStr = geocode.getString("location");
                String[] lngLat = locationStr.split(",");
                
                location.put("longitude", lngLat[0]);
                location.put("latitude", lngLat[1]);
                location.put("formatted_address", geocode.getString("formatted_address"));
                
                return location;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 计算两点间的直线距离
     * @param origins 起点经纬度，格式：lng,lat
     * @param destinations 终点经纬度，格式：lng,lat
     * @return 距离（米）
     */
    public static Integer calculateDistance(String origins, String destinations) {
        if (amapConfig == null) {
            return null;
        }
        
        String key = amapConfig.getKey();
        try {
            // 拼装url，使用高德地图路径规划API
            String url = amapConfig.getDistanceApiUrl() + "?key=" + key + "&origins=" + origins + "&destinations=" + destinations;
            String result = HttpClientUtils.doGet(url);
            JSONObject o = new JSONObject(result);
            
            // 检查返回状态
            String status = o.getString("status");
            if ("1".equals(status)) {
                JSONObject distance = o.getJSONArray("results").getJSONObject(0);
                return distance.getInt("distance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取配置的高德地图API密钥
     * @return 高德地图API密钥
     */
    public static String getKey() {
        return amapConfig != null ? amapConfig.getKey() : null;
    }
}