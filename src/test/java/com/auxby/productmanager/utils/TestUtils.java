package com.auxby.productmanager.utils;


import static com.auxby.productmanager.utils.constant.AppConstant.BASE_V1_URL;

public class TestUtils {
    public static String getUrl(String endpoint) {
        return String.format("/%s/%s", BASE_V1_URL, endpoint);
    }
}
