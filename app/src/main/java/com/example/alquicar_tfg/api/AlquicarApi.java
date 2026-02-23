package com.example.alquicar_tfg.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.google.gson.JsonObject; // Necesitarás importar esto

public interface AlquicarApi {

    // Llamamos al archivo login.php de Fernando
    @FormUrlEncoded
    @POST("login.php")
    Call<JsonObject> loginUsuario(
            @Field("correo") String correo
    );
}