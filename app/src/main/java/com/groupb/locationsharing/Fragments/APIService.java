package com.groupb.locationsharing.Fragments;

import com.groupb.locationsharing.Service.Notifications.MyResponse;
import com.groupb.locationsharing.Service.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers (
        {
            "Content-Type:application/json",
            "Authorization:key=AAAApJvIfCc:APA91bEbVKx-CrxDfp_InBqOQq7ODp8p-zLWV52XIS3EdH8c4QidyqWaKZWIrMTR2a1-My5INea-OhBheW8P00zc_C-y3OhxzxmnWrkEDLA-FKH91j2Akwd7TpMjQqpylhCsAz-WZFIb"
        }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotifications(@Body Sender body);
}
