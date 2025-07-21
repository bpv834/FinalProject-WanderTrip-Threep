package com.lion.wandertrip

import com.lion.wandertrip.repository.*
import com.lion.wandertrip.retrofit_for_practice.*
import com.lion.wandertrip.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitV1

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitV2

@Module
@InstallIn(SingletonComponent::class)
object TripAppModule {

    // User
    @Provides
    @Singleton
    fun userRepositoryProvider(): UserRepository = UserRepository()

    @Provides
    @Singleton
    fun userServiceProvider(userRepository: UserRepository): UserService =
        UserService(userRepository)

    // TripSchedule
    @Provides
    @Singleton
    fun tripScheduleRepositoryProvider(): TripScheduleRepository = TripScheduleRepository()

    @Provides
    @Singleton
    fun tripScheduleServiceProvider(
        tripScheduleRepository: TripScheduleRepository
    ): TripScheduleService = TripScheduleService(tripScheduleRepository)

    private const val BASE_URL = "http://apis.data.go.kr/B551011/KorService1/"

    // RetrofitV1
    @Provides
    @Singleton
    @RetrofitV1
    fun retrofitProvider(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // TripCommonItem
    @Provides
    @Singleton
    fun tripItemCommonInterfaceProvider(@RetrofitV1 retrofit: Retrofit): TripCommonItemInterface =
        retrofit.create(TripCommonItemInterface::class.java)

    @Provides
    @Singleton
    fun tripCommonItemRepositoryProvider(api: TripCommonItemInterface): TripCommonItemRepository =
        TripCommonItemRepository(api)

    @Provides
    @Singleton
    fun tripCommonItemServiceProvider(repository: TripCommonItemRepository): TripCommonItemService =
        TripCommonItemService(repository)

    // TripAreaBase
    @Provides
    @Singleton
    fun tripItemAreaBaseInterfaceProvider(@RetrofitV1 retrofit: Retrofit): TripAreaBaseItemInterface =
        retrofit.create(TripAreaBaseItemInterface::class.java)

    @Provides
    @Singleton
    fun tripAreaBaseItemRepositoryProvider(api: TripAreaBaseItemInterface): TripAreaBaseItemRepository =
        TripAreaBaseItemRepository(api)

    @Provides
    @Singleton
    fun tripAreaBaseItemServiceProvider(repository: TripAreaBaseItemRepository): TripAreaBaseItemService =
        TripAreaBaseItemService(repository)

    // TripKeywordItem
    @Provides
    @Singleton
    fun tripKeywordItemCommonInterfaceProvider(@RetrofitV1 retrofit: Retrofit): TripKeywordItemInterface =
        retrofit.create(TripKeywordItemInterface::class.java)

    @Provides
    @Singleton
    fun tripKeywordItemRepositoryProvider(api: TripKeywordItemInterface): TripKeywordItemRepository =
        TripKeywordItemRepository(api)

    @Provides
    @Singleton
    fun tripKeywordItemServiceProvider(repository: TripKeywordItemRepository): TripKeywordItemService =
        TripKeywordItemService(repository)

    // Contents
    @Provides
    @Singleton
    fun contentsRepositoryProvider(): ContentsRepository = ContentsRepository()

    @Provides
    @Singleton
    fun contentsServiceProvider(contentsRepository: ContentsRepository): ContentsService =
        ContentsService(contentsRepository)

    // ContentsReview
    @Provides
    @Singleton
    fun contentsReviewRepositoryProvider(): ContentsReviewRepository = ContentsReviewRepository()

    @Provides
    @Singleton
    fun contentsReviewServiceProvider(contentsReviewRepository: ContentsReviewRepository): ContentsReviewService =
        ContentsReviewService(contentsReviewRepository)

    // TripLocationBased
    @Provides
    @Singleton
    fun tripLocationBasedInterfaceProvider(@RetrofitV1 retrofit: Retrofit): TripLocationBasedInterface =
        retrofit.create(TripLocationBasedInterface::class.java)

    @Provides
    @Singleton
    fun tripLocationBasedRepositoryProvider(api: TripLocationBasedInterface): TripLocationBasedItemRepository =
        TripLocationBasedItemRepository(api)

    @Provides
    @Singleton
    fun tripLocationBasedServiceProvider(repository: TripLocationBasedItemRepository): TripLocationBasedItemService =
        TripLocationBasedItemService(repository)

    // RetrofitV2 for AreaBaseItem2
    private const val BASE_URL_V2 = "https://apis.data.go.kr/B551011/KorService2/"

    @Provides
    @Singleton
    @RetrofitV2
    fun retrofitProvider2(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_V2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun tripItemAreaBaseInterfaceProvider2(@RetrofitV2 retrofit: Retrofit): TripAreaBaseItem2Interface =
        retrofit.create(TripAreaBaseItem2Interface::class.java)

    @Provides
    @Singleton
    fun tripItemAreaBaseRepositoryProvider2(api: TripAreaBaseItem2Interface): TripAreaBaseItem2Repository =
        TripAreaBaseItem2Repository(api)

    @Provides
    @Singleton
    fun tripItemAreaBaseServiceProvider2(repository: TripAreaBaseItem2Repository): TripAreaBaseItem2Service =
        TripAreaBaseItem2Service(repository)
}
