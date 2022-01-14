package com.aitymkiv.googlemapstestproject

import com.aitymkiv.googlemapstestproject.model.MainJsonObject
import com.aitymkiv.googlemapstestproject.model.Point
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiInterface {
    @GET("getPoint")
    fun getPoint(): Call<MainJsonObject>

}