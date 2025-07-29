package com.lion.wandertrip.di

import com.lion.wandertrip.retrofit_for_practice.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// Hilt는 @Module이 붙은 객체나 클래스를 직접 인식합니다.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL_V1 = "http://apis.data.go.kr/B551011/KorService1/"
    private const val BASE_URL_V2 = "https://apis.data.go.kr/B551011/KorService2/"

    @Provides
    @Singleton
    @RetrofitV1
    fun provideRetrofitV1(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_V1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @RetrofitV2
    fun provideRetrofitV2(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_V2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTripCommonItemApi(@RetrofitV2 retrofit: Retrofit): TripCommonItemInterface =
        retrofit.create(TripCommonItemInterface::class.java)

    @Provides
    @Singleton
    fun provideTripAreaBaseItemApi(@RetrofitV2 retrofit: Retrofit): TripAreaBaseItemInterface =
        retrofit.create(TripAreaBaseItemInterface::class.java)

    @Provides
    @Singleton
    fun provideTripKeywordItemApi(@RetrofitV2 retrofit: Retrofit): TripKeywordItemInterface =
        retrofit.create(TripKeywordItemInterface::class.java)

    @Provides
    @Singleton
    fun provideTripLocationBasedApi(@RetrofitV2 retrofit: Retrofit): TripLocationBasedInterface =
        retrofit.create(TripLocationBasedInterface::class.java)

    @Provides
    @Singleton
    fun provideTripAreaBaseItem2Api(@RetrofitV2 retrofit: Retrofit): TripAreaBaseItem2Interface =
        retrofit.create(TripAreaBaseItem2Interface::class.java)
}
