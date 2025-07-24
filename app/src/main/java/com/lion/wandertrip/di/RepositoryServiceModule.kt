package com.lion.wandertrip.di

import com.lion.wandertrip.repository.*
import com.lion.wandertrip.retrofit_for_practice.*
import com.lion.wandertrip.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Module에 @InstallIn(SingletonComponent::class)가 붙어 있으면
//애플리케이션 전체 범위에서 자동으로 DI 컨테이너에 등록됩니다.
@Module
@InstallIn(SingletonComponent::class)
object RepositoryServiceModule {

    // User
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = UserRepository()

    @Provides
    @Singleton
    fun provideUserService(userRepository: UserRepository): UserService =
        UserService(userRepository)

    // TripSchedule
    @Provides
    @Singleton
    fun provideTripScheduleRepository(): TripScheduleRepository = TripScheduleRepository()

    @Provides
    @Singleton
    fun provideTripScheduleService(tripScheduleRepository: TripScheduleRepository): TripScheduleService =
        TripScheduleService(tripScheduleRepository)

    // TripCommonItem
    @Provides
    @Singleton
    fun provideTripCommonItemRepository(api: TripCommonItemInterface): TripCommonItemRepository =
        TripCommonItemRepository(api)

    @Provides
    @Singleton
    fun provideTripCommonItemService(repository: TripCommonItemRepository): TripCommonItemService =
        TripCommonItemService(repository)

    // TripAreaBaseItem
    @Provides
    @Singleton
    fun provideTripAreaBaseItemRepository(api: TripAreaBaseItemInterface): TripAreaBaseItemRepository =
        TripAreaBaseItemRepository(api)

    @Provides
    @Singleton
    fun provideTripAreaBaseItemService(repository: TripAreaBaseItemRepository): TripAreaBaseItemService =
        TripAreaBaseItemService(repository)

    // TripKeywordItem
    @Provides
    @Singleton
    fun provideTripKeywordItemRepository(api: TripKeywordItemInterface): TripKeywordItemRepository =
        TripKeywordItemRepository(api)

    @Provides
    @Singleton
    fun provideTripKeywordItemService(repository: TripKeywordItemRepository): TripKeywordItemService =
        TripKeywordItemService(repository)

    // Contents
    @Provides
    @Singleton
    fun provideContentsRepository(): ContentsRepository = ContentsRepository()

    @Provides
    @Singleton
    fun provideContentsService(repository: ContentsRepository): ContentsService =
        ContentsService(repository)

    // ContentsReview
    @Provides
    @Singleton
    fun provideContentsReviewRepository(): ContentsReviewRepository = ContentsReviewRepository()

    @Provides
    @Singleton
    fun provideContentsReviewService(repository: ContentsReviewRepository): ContentsReviewService =
        ContentsReviewService(repository)

    // TripLocationBased
    @Provides
    @Singleton
    fun provideTripLocationBasedRepository(api: TripLocationBasedInterface): TripLocationBasedItemRepository =
        TripLocationBasedItemRepository(api)

    @Provides
    @Singleton
    fun provideTripLocationBasedService(repository: TripLocationBasedItemRepository): TripLocationBasedItemService =
        TripLocationBasedItemService(repository)

    // TripAreaBaseItem2
    @Provides
    @Singleton
    fun provideTripAreaBaseItem2Repository(api: TripAreaBaseItem2Interface): TripAreaBaseItem2Repository =
        TripAreaBaseItem2Repository(api)

    @Provides
    @Singleton
    fun provideTripAreaBaseItem2Service(repository: TripAreaBaseItem2Repository): TripAreaBaseItem2Service =
        TripAreaBaseItem2Service(repository)
}