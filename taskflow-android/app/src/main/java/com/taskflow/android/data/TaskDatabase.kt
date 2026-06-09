package com.taskflow.android.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 2, exportSchema = true)
@TypeConverters(TaskConverters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tasks ADD COLUMN updatedAt TEXT NOT NULL DEFAULT ''")
                // Back-fill: use createdAt as updatedAt, id-based syncId for existing rows
                db.execSQL("UPDATE tasks SET updatedAt = createdAt WHERE updatedAt = ''")
                db.execSQL("UPDATE tasks SET syncId = 'legacy_' || id WHERE syncId = ''")
            }
        }
    }
}
