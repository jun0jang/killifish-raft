package jraft.common.network

import java.nio.ByteBuffer

/**
 * 버퍼 사이즈를 어떻게 잡아야하누 ?
 */
class HttpReceive : Receive {
    enum class ParseState {
        HEAD_LINE,
        HEADERS,
        BODY,
    }

    private var state = ParseState.HEAD_LINE

    private val readChar = ByteBuffer.allocate(1)

    private var lineBuffer = byteArrayOf()

    private var bytes = byteArrayOf()

    private var zeroRnLn: Boolean = false

    private var rnLn = false

    private var completed = false

    override fun completed(): Boolean {
        return completed
    }

    override fun readFrom(transportLayer: TransportLayer): Long {
        var result = 0

        while (true) {
            val (readCount, line) = readLine(transportLayer)
            if (readCount < 0) {
                return -1
            }
            if (readCount == 0) {
                break
            }
            result += readCount

            if (line == null) {
                break
            }
            bytes = bytes.plus(line)

            when (state) {
                ParseState.HEAD_LINE -> {
                    state = ParseState.HEADERS
                }
                ParseState.HEADERS -> {
                    if (line.contentEquals("\r\n".toByteArray())) {
                        state = ParseState.BODY
                    }
                }
                ParseState.BODY -> {
                    if (line.contentEquals("0\r\n".toByteArray())) {
                        zeroRnLn = true
                    } else if (line.contentEquals("\r\n".toByteArray())) {
                        rnLn = true
                    } else {
                        zeroRnLn = false
                        rnLn = false
                    }

                    if (zeroRnLn && rnLn) {
                        completed = true
                        break
                    }
                }
            }
        }

        return result.toLong()
    }

    private fun readLine(transportLayer: TransportLayer): Pair<Int, ByteArray?> {
        var result = 0
        while (true) {
            val read = transportLayer.read(readChar)
            if (read < 0) {
                transportLayer.disconnect()
                return -1 to null
            }
            if (read == 0) {
                return result to null
            }

            result += read
            lineBuffer = lineBuffer.plus(readChar.array())

            readChar.flip()
            if (readChar.array().contentEquals("\n".toByteArray())) {
                val line = lineBuffer
                lineBuffer = byteArrayOf()
                return result to line
            }
        }
    }

    override fun payload(): ByteBuffer {
        val buffer = ByteBuffer.allocate(bytes.size)
        bytes.forEach { buffer.put(it) }
        buffer.flip()
        return buffer
    }
}
