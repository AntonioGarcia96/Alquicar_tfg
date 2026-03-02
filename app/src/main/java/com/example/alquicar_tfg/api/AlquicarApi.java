package com.example.alquicar_tfg.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.google.gson.JsonObject;

public interface AlquicarApi {

    //Llamamos al archivo login.php de Fernando
    @FormUrlEncoded
    @POST("login.php")
    Call<JsonObject> loginUsuario(
            @Field("correo") String correo
    );

    //Llamamos al archivo de registro
    @FormUrlEncoded
    @POST("registro.php")
    Call<JsonObject> registrarUsuario(
            @Field("dni") String dni,
            @Field("nombre") String nombre,
            @Field("apellidos") String apellidos,
            @Field("fecha_nacimiento") String fecha_nacimiento,
            @Field("direccion") String direccion,
            @Field("telefono") String telefono,
            @Field("email") String email,
            @Field("cuenta_bancaria") String cuentaBancaria,
            @Field("contrasenna") String contrasenna


    );
}