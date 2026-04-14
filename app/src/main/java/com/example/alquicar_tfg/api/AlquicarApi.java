package com.example.alquicar_tfg.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.google.gson.JsonObject;

import java.util.List;

public interface AlquicarApi {

    @FormUrlEncoded
    @POST("obtener_historial.php")
    Call<List<JsonObject>> obtenerHistorial(@Field("id_cliente") String idCliente);
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
    @FormUrlEncoded
    @POST("login.php")
    Call<JsonObject> loginUsuario(
            @Field("email") String email,
            @Field("contrasenna") String contrasenna
    );

    // Este sirve para leer los datos (Tanto en PerfilActivity como en EditarPerfilActivity)
    @FormUrlEncoded
    @POST("obtener_perfil.php")
    Call<JsonObject> obtenerPerfil(@Field("id_cliente") String idCliente);

    // Este sirve para guardar los cambios
    @FormUrlEncoded
    @POST("actualizar_perfil.php")
    Call<JsonObject> actualizarPerfil(
            @Field("id_cliente") String idCliente,
            @Field("nombre") String nombre,
            @Field("apellidos") String apellidos,
            @Field("direccion") String direccion,
            @Field("email") String email,
            @Field("contrasenna") String contrasenna,
            @Field("cuenta_bancaria") String cuentaBancaria
    );

    // Llamada para guardar un viaje cuando el usuario le da a "Terminar"
    @FormUrlEncoded
    @POST("registrar_viaje.php")
    Call<JsonObject> registrarViaje(
            @Field("id_cliente") String idCliente,
            @Field("id_vehiculo") int idVehiculo, // NUEVO
            @Field("modalidad") String modalidad, // NUEVO
            @Field("recorrido") double recorrido,
            @Field("coste") double coste,
            @Field("co2") int co2
    );
}