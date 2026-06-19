package com.mandrecode.tempo.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InMemoryTempoDatabaseRule : TestWatcher() {
    lateinit var database: TempoDatabase
        private set

    override fun starting(description: Description) {
        super.starting(description)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, TempoDatabase::class.java)
                .build()
    }

    override fun finished(description: Description) {
        if (::database.isInitialized) {
            database.close()
        }
        super.finished(description)
    }
}
