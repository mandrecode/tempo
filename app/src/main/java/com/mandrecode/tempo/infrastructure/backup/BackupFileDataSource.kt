package com.mandrecode.tempo.infrastructure.backup

import android.content.Context
import android.net.Uri
import com.mandrecode.tempo.core.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream

/**
 * Reads and writes backup documents at SAF (`content://`) URIs picked by the
 * user via the system file picker. All provider failures — including the
 * [SecurityException]/[IllegalArgumentException] the resolver can throw for
 * revoked or invalid URIs — surface as [IOException] so callers have a single
 * error contract.
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
            asIoFailure {
                openTruncatingOutputStream(uri).bufferedWriter().use { it.write(content) }
            }
        }

        /**
         * Opens the destination in a truncating mode — plain "w" would leave the
         * tail of a longer previous file in place, corrupting the JSON. SAF
         * document providers accept "wt" ([android.os.ParcelFileDescriptor.parseMode]);
         * providers that only take the modes documented on
         * [android.content.ContentResolver.openOutputStream] get the "rwt" fallback,
         * which also truncates.
         */
        private fun openTruncatingOutputStream(uri: Uri): OutputStream {
            val stream =
                try {
                    context.contentResolver.openOutputStream(uri, "wt")
                } catch (_: IllegalArgumentException) {
                    context.contentResolver.openOutputStream(uri, "rwt")
                } catch (_: UnsupportedOperationException) {
                    context.contentResolver.openOutputStream(uri, "rwt")
                }
            return stream ?: throw IOException("Cannot open $uri for writing")
        }

        suspend fun read(uri: Uri): String =
            withContext(ioDispatcher) {
                asIoFailure {
                    val stream =
                        context.contentResolver.openInputStream(uri)
                            ?: throw IOException("Cannot open $uri for reading")
                    stream.bufferedReader().use { it.readText() }
                }
            }

        private inline fun <T> asIoFailure(block: () -> T): T =
            try {
                block()
            } catch (e: SecurityException) {
                throw IOException(e)
            } catch (e: IllegalArgumentException) {
                throw IOException(e)
            }
    }
