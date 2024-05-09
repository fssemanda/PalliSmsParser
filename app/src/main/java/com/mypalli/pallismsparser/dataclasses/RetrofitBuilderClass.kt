package com.mypalli.pallismsparser.dataclasses

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilderClass {
    companion object{
//                const val BASE_URL ="http://192.168.2.109:8000/"
//        const val BASE_URL ="https://pangisha.ledge.ug/"
//        const val BASE_URL ="http://192.168.78.186:8000/"
//        const val BASE_URL ="http://192.168.1.101:8000/"
        const val BASE_URL ="https://palli-app2-kprtnean4a-no.a.run.app/"

        fun getRetroInstance(): Retrofit {

            val logging =  HttpLoggingInterceptor()
            logging.level = (HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
            client.addInterceptor(logging)
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

    }
}