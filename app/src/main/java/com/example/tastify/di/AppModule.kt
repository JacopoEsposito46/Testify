package com.example.tastify.di

import com.example.tastify.data.AnalyticsRepository
import com.example.tastify.data.AuthRepository
import com.example.tastify.data.ChatLocalRepository
import com.example.tastify.data.CommentRepository
import com.example.tastify.data.CustomCoursesRepository
import com.example.tastify.data.FavoritesRepository
import com.example.tastify.data.FirebaseAnalyticsRepository
import com.example.tastify.data.FirebaseAuthRepository
import com.example.tastify.data.FirebaseCommentRepository
import com.example.tastify.data.FirebaseCustomCoursesRepository
import com.example.tastify.data.FirebaseFavoritesRepository
import com.example.tastify.data.FirebaseHistoryRepository
import com.example.tastify.data.FirebaseIngredientRepository
import com.example.tastify.data.FirebaseNotificationRepository
import com.example.tastify.data.FirebaseProfileRepository
import com.example.tastify.data.FirebaseRecipeRepository
import com.example.tastify.data.FirebaseRecipeAnalyticsRepository
import com.example.tastify.data.FirebaseReportRepository
import com.example.tastify.data.FirebaseReviewRepository
import com.example.tastify.data.FirebaseWeeklyPlanRepository
import com.example.tastify.data.HistoryRepository
import com.example.tastify.data.IngredientRepository
import com.example.tastify.data.NotificationRepository
import com.example.tastify.data.RecipeAnalyticsRepository
import com.example.tastify.data.ProfileRepository
import com.example.tastify.data.RecipeRepository
import com.example.tastify.data.ReportRepository
import com.example.tastify.data.ReviewRepository
import com.example.tastify.data.WeeklyPlanRepository
import com.example.tastify.data.local.ChatLocalRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        impl: FirebaseRecipeRepository
    ): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindRecipeAnalyticsRepository(
        impl: FirebaseRecipeAnalyticsRepository
    ): RecipeAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: FirebaseCommentRepository
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: FirebaseReviewRepository
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: FirebaseReportRepository
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: FirebaseHistoryRepository
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindCustomCoursesRepository(
        impl: FirebaseCustomCoursesRepository
    ): CustomCoursesRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: FirebaseNotificationRepository
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: FirebaseProfileRepository
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindIngredientRepository(
        impl: FirebaseIngredientRepository
    ): IngredientRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        impl: FirebaseFavoritesRepository
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: FirebaseAnalyticsRepository
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindWeeklyPlanRepository(
        impl: FirebaseWeeklyPlanRepository
    ): WeeklyPlanRepository

    @Binds
    @Singleton
    abstract fun bindChatLocalRepository(
        impl: ChatLocalRepositoryImpl
    ): ChatLocalRepository

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }
    }
}