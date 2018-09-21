package com.fs.locationdemo;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Locale;

import static com.fs.locationdemo.Utils.isNullText;

public class JsonUtils {

    public static JSONObject getCommLocation(Context context) {
        JSONObject jsonObject = new JSONObject();
        if (Utils.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //获取所有可用的位置提供器
            List<String> providers = locationManager.getProviders(true);
            // TODO: 2018/6/29 需要开启VPN 并且需要GP服务在手机内
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                // 查找到服务信息
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

                String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
                Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
                double latitude = -1;
                double longitude = -1;
                if (null != location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    JSONObject gpsObj = new JSONObject();
                    gpsObj.put("latitude", latitude);
                    gpsObj.put("longitude", longitude);
                    jsonObject.put("gps", gpsObj);
                } else {
                    location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
                    if (null != location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        JSONObject netObj = new JSONObject();
                        netObj.put("latitude", latitude);
                        netObj.put("longitude", longitude);
                        jsonObject.put("network", netObj);
                    }
                }
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                    LogUtil.e("addresses size: " + addresses.size());
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String country = address.getCountryName();
                        String province = address.getAdminArea();
                        String city = address.getSubAdminArea();
                        String bigDirect = address.getLocality();
                        String smallDirect = address.getThoroughfare();
                        String detailed = address.getAddressLine(0);
                        jsonObject.put("gps_address_province", isNullText(province));
                        jsonObject.put("gps_address_city", isNullText(city));
                        jsonObject.put("gps_address_large_district", isNullText(bigDirect));
                        jsonObject.put("gps_address_small_district", isNullText(smallDirect));
                        jsonObject.put("gps_address_street", isNullText(detailed));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Criteria criteria = new Criteria();
                criteria.setCostAllowed(false);
                //设置位置服务免费
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(false);
                criteria.setPowerRequirement(Criteria.POWER_LOW);

                //getBestProvider 只有允许访问调用活动的位置供应商将被返回
                String providerName = locationManager.getBestProvider(criteria, true);

                //LogUtil.INSTANCE.e("providerName: " + providerName);
                if (providerName != null) {
                    Location location = locationManager.getLastKnownLocation(providerName);
                    if (location != null) {
                        JSONObject gpsObj = new JSONObject();
                        try {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            LogUtil.e("latitude: " + latitude + ", longitude: " + longitude);

                            JSONObject locaObj = new JSONObject();
                            locaObj.put("latitude", latitude);
                            locaObj.put("longitude", longitude);
                            jsonObject.put("network", locaObj);

                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            LogUtil.e("addresses size: " + addresses.size());
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                String country = address.getCountryName();
                                String province = address.getAdminArea();
                                String city = address.getSubAdminArea();
                                String bigDirect = address.getLocality();
                                String smallDirect = address.getThoroughfare();
                                String detailed = address.getAddressLine(0);

                                jsonObject.put("gps_address_province", isNullText(province));
                                jsonObject.put("gps_address_city", isNullText(city));
                                jsonObject.put("gps_address_large_district", isNullText(bigDirect));
                                jsonObject.put("gps_address_small_district", isNullText(smallDirect));
                                jsonObject.put("gps_address_street", isNullText(detailed));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    return null;
                }
            }
        }
        //获取Location
        return jsonObject;
    }
}
