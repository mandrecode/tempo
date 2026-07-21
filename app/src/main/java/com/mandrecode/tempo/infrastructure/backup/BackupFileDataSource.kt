package com.mandrecode.tempo.infrastructure.backup

import android.content.Context
import android.net.Uri
import com.mandrecode.tempo.core.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Reads and writes backup documents at SAF (`content://`) URIs picked by the
 * user via the system file picker. Throws [IOException] when the provider
 * rejects the operation.
 */
class BackupFileDataSource
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        suspend fun write(
            uri: Uri,
            content: String,
        ) = withContext(ioDispatcher) {
            val stream =
                context.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IOException("Cannot open $uri for writing")
            stream.bufferedWriter().use { it.write(content) }
        }

        suspend fun read(uri: Uri): String =
            withContext(ioDispatcher) {
                val stream =
                    context.contentResolver.openInputStream(uri)
                        ?: throw IOException("Cannot open $uri for reading")
                stream.bufferedReader().use { it.readText() }
            }
    }
