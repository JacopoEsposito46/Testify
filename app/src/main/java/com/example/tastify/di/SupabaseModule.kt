package com.example.tastify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val supabaseUrl = "https://gmaqfkfftrxwpkirdseg.supabase.co"
        val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdtYXFma2ZmdHJ4d3BraXJkc2VnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk0NjAwMzYsImV4cCI6MjA5NTAzNjAzNn0.SxePneI0KFDE_RoWj4DKO3P-Y_JIffgxfj7w6m_apU8"

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Storage)
        }
    }
}