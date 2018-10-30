package com.github.kittinunf.fuel.util

import java.io.BufferedInputStream
import java.io.InputStream

typealias ReadProgress = (Long) -> Unit

/**
 * Stream that reports read progress upon efficient reads.
 *
 * @see ProgressOutputStream
 *
 * @note the callback is called as many times as the [inputStream] consumer is calling read. When this stream is used
 *   to report progress back to a user, you probably want to tween between values, to make it look like there is more
 *   frequent activity, although this actually represents the actual progress.
 *
 * @param stream [InputStream] the stream that should have progress reporting
 * @param onProgress [ReadProgress] the progress callback
 */
class ProgressInputStream(stream: InputStream, val onProgress: ReadProgress) : BufferedInputStream(stream) {
    private var position = 0L

    override fun reset() {
        super.reset()
        // Going back at most current buffer position - marked buffer position
        position -= (pos - markpos)
    }

    override fun skip(n: Long): Long {
        return super.skip(n).apply {
            position + n
        }
    }

    // Report progress if the consumer is efficient.
    //
    // This means that the amount of times the progress is reported, ties exactly into the number of times the `read`
    // function is called, instead of relying on some arbitrary, but fake, progress.
    //
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        return super.read(b, off, len).apply {
            position += Math.max(this, 0)
            onProgress.invoke(position)
        }
    }
}