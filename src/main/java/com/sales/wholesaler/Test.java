package com.sales.wholesaler;

import org.json.JSONObject;

public class Test {

    public static void main(String[] args) {

        String subjectCodeValue = "AB857";
        String setCodeJsonStr = "{"
                + "\"AB857\":\"589,590,591,592\","
                + "\"AB858\":\"989,990,991,992\","
                + "\"AB855\":\"889,890,891,892\","
                + "\"AB85787\":\"689,690,791,792\""
                + "}";
        JSONObject jsonObject = new JSONObject(setCodeJsonStr);

        String setCodes = "";
        if (jsonObject.has(subjectCodeValue)) {
            setCodes = jsonObject.getString(subjectCodeValue);
        }
        //String setCodes = projectParams.getString("set_codes");
        String setCodesArr[] = setCodes.split(",", -1); // -1 used for if during slipt have comma(,) in last. then don't get blank value. like a,b,c, -> [a,b,c]

        for (int i = 0; i < setCodesArr.length; i++) {
            System.out.println(setCodesArr[i]);
        }

    }

}
