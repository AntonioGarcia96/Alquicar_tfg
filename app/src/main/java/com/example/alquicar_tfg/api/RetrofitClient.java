package com.example.alquicar_tfg.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 10.0.2.2 es la IP mágica que usa el emulador de Android para referirse al "localhost" de tu ordenador
    private static final String BASE_URL = "http://10.0.2.2/alquicar_api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}