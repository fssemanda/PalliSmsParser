package com.mypalli.pallismsparser.dataclasses

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilderClass {
    companion object{

        //Dev link
//        const val BASE_URL ="https://palli-production-kprtnean4a-no.a.run.app/"
        //Production
        const val BASE_URL ="https://palli-app3-302984642881.europe-southwest1.run.app/"
//        const val BASE_URL ="https://palli-app2-302984642881.europe-southwest1.run.app/"


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

//match = re.search(r'code :(\d+)', text)
//if match:
//code = match.group(1)  # This will give you "2864"
//print(code)