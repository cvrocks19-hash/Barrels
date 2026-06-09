package com.taskflow.android.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.taskflow.android.data.TaskDao
import com.taskflow.android.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): TaskDatabase =
        Room.databaseBuilder(ctx, TaskDatabase::class.java, "taskflow.db")
            .addMigrations(TaskDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.taskDao()

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().also { db ->
        // Enable offline persistence so the app works without internet
        db.firestoreSettings = com.google.firebase.firestore.firestoreSettings {
            isPersistenceEnabled = true
        }
    }
}
