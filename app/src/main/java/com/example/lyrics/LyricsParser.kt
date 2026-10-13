package com.example.lyrics

import com.example.model.LyricLine
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Robust Multi-Language Lyrics & Encoding Parser.
 * Resolves mojibake/gibberish text caused by non-UTF-8 audio tags, HTML entities,
 * or legacy encodings across Asian, Indic, Arabic, Cyrillic, and European scripts.
 */
object LyricsParser {

    private val BOM_UTF8 = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private val BOM_UTF16_LE = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    private val BOM_UTF16_BE = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

    private val LRC_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{2,3}))?\\](.*)")
    private val INLINE_TIMESTAMP_PATTERN = Pattern.compile("<\\d{1,2}:\\d{2}(?:[.:]\\d{2,3})?>")
    private val HTML_ENTITY_PATTERN = Pattern.compile("&(#?[a-zA-Z0-9]+);")

    /**
     * Decodes a raw byte array into a String by inspecting the BOM and testing charsets.
     * Prevents gibberish characters (mojibake) from Chinese, Japanese, Korean, Hindi, or legacy encodings.
     */
    fun decodeBytesWithCharsetAutoDetect(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        // 1. Check Byte Order Mark (BOM)
        if (bytes.size >= 3 && bytes[0] == BOM_UTF8[0] && bytes[1] == BOM_UTF8[1] && bytes[2] == BOM_UTF8[2]) {
            return sanitizeText(String(bytes, 3, bytes.size - 3, StandardCharsets.UTF_8))
        }
        if (bytes.size >= 2 && bytes[0] == BOM_UTF16_LE[0] && bytes[1] == BOM_UTF16_LE[1]) {
            return sanitizeText(String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16LE))
        }
        if (bytes.size >= 2 && bytes[0] == BOM_UTF16_BE[0] && bytes[1] == BOM_UTF16_BE[1]) {
            return sanitizeText(String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16BE))
        }

        // 2. Check for UTF-16 without BOM (e.g. alternating null bytes for ASCII characters)
        if (bytes.size >= 4) {
            val isLePattern = bytes[1] == 0.toByte() && bytes[3] == 0.toByte()
            val isBePattern = bytes[0] == 0.toByte() && bytes[2] == 0.toByte()
            if (isLePattern && !isBePattern) {
                try {
                    val decoded = String(bytes, StandardCharsets.UTF_16LE)
                    if (!decoded.contains('\uFFFD') && decoded.any { it.isLetterOrDigit() }) {
                        return sanitizeText(decoded)
                    }
                } catch (_: Exception) {}
            } else if (isBePattern && !isLePattern) {
                try {
                    val decoded = String(bytes, StandardCharsets.UTF_16BE)
                    if (!decoded.contains('\uFFFD') && decoded.any { it.isLetterOrDigit() }) {
                        return sanitizeText(decoded)
                    }
                } catch (_: Exception) {}
            }
        }

        // 3. Attempt strict UTF-8 decoding
        try {
            val utf8Decoder = StandardCharsets.UTF_8.newDecoder()
            val charBuffer = utf8Decoder.decode(java.nio.ByteBuffer.wrap(bytes))
            val result = charBuffer.toString()
            if (!result.contains('\uFFFD')) {
                return sanitizeText(result)
            }
        } catch (_: Exception) {
            // Not valid UTF-8, proceed to fallbacks
        }

        // 4. Fallback to candidate multi-byte & regional charsets
        val candidateCharsets = listOf(
            "GB18030",     // Covers GBK, GB2312, and Chinese/Japanese/Korean
            "Big5",        // Traditional Chinese
            "Shift_JIS",   // Japanese
            "EUC-KR",      // Korean
            "windows-1251",// Cyrillic / Russian
            "windows-1256",// Arabic
            "windows-1252",// Western Europe / English
            "ISO-8859-1"
        )

        for (charsetName in candidateCharsets) {
            try {
                if (Charset.isSupported(charsetName)) {
                    val charset = Charset.forName(charsetName)
                    val decoder = charset.newDecoder()
                    val decoded = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
                    if (!decoded.contains('\uFFFD')) {
                        return sanitizeText(decoded)
                    }
                }
            } catch (_: Exception) {
                // Continue trying other candidate charsets
            }
        }

        // Final graceful fallback
        return sanitizeText(String(bytes, StandardCharsets.UTF_8))
    }

    /**
     * Decodes ID3v2 USLT (Unsynchronized lyrics/text transcription) frame bytes properly.
     */
    fun decodeId3UsltFrame(framePayload: ByteArray): String {
        if (framePayload.isEmpty()) return ""

        val encodingByte = framePayload[0].toInt() and 0xFF
        // Skip 1 byte encoding + 3 bytes language descriptor (e.g. "eng", "xxx")
        var offset = 4
        if (offset >= framePayload.size) return ""

        val charset = when (encodingByte) {
            0 -> Charset.forName("ISO-8859-1")
            1 -> StandardCharsets.UTF_16 // With BOM or default
            2 -> StandardCharsets.UTF_16BE
            3 -> StandardCharsets.UTF_8
            else -> StandardCharsets.UTF_8
        }

        // Skip null-terminated content descriptor
        if (encodingByte == 1 || encodingByte == 2) {
            // 2-byte null terminator (0x00 0x00)
            while (offset + 1 < framePayload.size) {
                if (framePayload[offset] == 0.toByte() && framePayload[offset + 1] == 0.toByte()) {
                    offset += 2
                    break
                }
                offset += 2
            }
        } else {
            // 1-byte null terminator (0x00)
            while (offset < framePayload.size) {
                if (framePayload[offset] == 0.toByte()) {
                    offset += 1
                    break
                }
                offset += 1
            }
        }

        if (offset >= framePayload.size) {
            offset = 4
        }

        val textBytes = framePayload.copyOfRange(offset, framePayload.size)
        return try {
            val raw = String(textBytes, charset)
            sanitizeText(raw)
        } catch (_: Exception) {
            decodeBytesWithCharsetAutoDetect(textBytes)
        }
    }

    /**
     * Parses an LRC string into a list of sorted [LyricLine] instances.
     * Supports formats like [01:23.45], [01:23.456], and [01:23:45].
     */
    fun parseLrc(lrcContent: String): List<LyricLine> {
        if (lrcContent.isBlank()) return emptyList()

        val sanitizedContent = sanitizeText(lrcContent)
        val lines = mutableListOf<LyricLine>()
        sanitizedContent.lines().forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isNotBlank()) {
                val matcher = LRC_PATTERN.matcher(trimmed)
                if (matcher.matches()) {
                    val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                    val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                    val fracStr = matcher.group(3) ?: "0"
                    val fractionMs = when (fracStr.length) {
                        1 -> fracStr.toLong() * 100
                        2 -> fracStr.toLong() * 10
                        3 -> fracStr.toLong()
                        else -> 0L
                    }
                    val timestampMs = (minutes * 60 + seconds) * 1000 + fractionMs
                    val rawText = matcher.group(4)?.trim() ?: ""
                    val cleanText = sanitizeLineText(rawText)
                    if (cleanText.isNotBlank()) {
                        lines.add(LyricLine(timestampMs, cleanText))
                    }
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    /**
     * Decodes raw bytes and parses them into [LyricLine]s.
     */
    fun parseLrcBytes(bytes: ByteArray): List<LyricLine> {
        val decodedText = decodeBytesWithCharsetAutoDetect(bytes)
        return parseLrc(decodedText)
    }

    /**
     * Sanitizes lyrics text by unescaping HTML entities, repairing UTF-8 mojibake,
     * stripping control characters, and normalizing typography symbols.
     */
    fun sanitizeText(raw: String): String {
        if (raw.isBlank()) return raw

        var text = raw

        // 1. Repair common UTF-8 mojibake (double-decoded bytes)
        text = fixCommonMojibake(text)

        // 2. Decode HTML and numeric entities (e.g. &#39; -> ', &amp; -> &, &quot; -> ")
        text = unescapeHtmlEntities(text)

        // 3. Remove non-printable control characters except standard newlines and tabs
        text = text.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")

        return text.trim()
    }

    /**
     * Sanitizes an individual lyric line by stripping inline karaoke timestamp tags
     * (e.g. <00:12.34>) and unescaping entities.
     */
    private fun sanitizeLineText(rawLine: String): String {
        var line = rawLine
        line = INLINE_TIMESTAMP_PATTERN.matcher(line).replaceAll("")
        line = sanitizeText(line)
        return line
    }

    /**
     * Unescapes HTML entities including named, decimal (&#39;) and hex (&#x27;) entities.
     */
    fun unescapeHtmlEntities(input: String): String {
        if (!input.contains('&')) return input

        var result = input
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#039;", "'")
            .replace("&#x27;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")
            .replace("&hellip;", "…")
            .replace("&rsquo;", "’")
            .replace("&lsquo;", "‘")
            .replace("&rdquo;", "”")
            .replace("&ldquo;", "“")
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")

        // Numeric entity regex resolver for any remaining numeric entities
        val matcher = HTML_ENTITY_PATTERN.matcher(result)
        val sb = StringBuffer()
        while (matcher.find()) {
            val entity = matcher.group(1) ?: ""
            val replacement = if (entity.startsWith("#x", ignoreCase = true)) {
                try {
                    val codePoint = entity.substring(2).toInt(16)
                    String(Character.toChars(codePoint))
                } catch (_: Exception) {
                    matcher.group(0) ?: ""
                }
            } else if (entity.startsWith("#")) {
                try {
                    val codePoint = entity.substring(1).toInt(10)
                    String(Character.toChars(codePoint))
                } catch (_: Exception) {
                    matcher.group(0) ?: ""
                }
            } else {
                matcher.group(0) ?: ""
            }
            matcher.appendReplacement(sb, MatcherQuoteReplacement(replacement))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    /**
     * Helper to safely escape replacement strings for regex appendReplacement.
     */
    private fun MatcherQuoteReplacement(s: String): String {
        return java.util.regex.Matcher.quoteReplacement(s)
    }

    /**
     * Repairs common mojibake sequences that happen when UTF-8 strings are incorrectly read as Windows-1252.
     */
    private fun fixCommonMojibake(input: String): String {
        if (!input.contains('Ã') && !input.contains('â')) return input

        return input
            .replace("â€™", "’")
            .replace("â€˜", "‘")
            .replace("â€œ", "“")
            .replace("â€\u009D", "”")
            .replace("â€\"", "”")
            .replace("â€”", "—")
            .replace("â€“", "–")
            .replace("â€¦", "…")
            .replace("Ã©", "é")
            .replace("Ã¨", "è")
            .replace("Ã ", "à")
            .replace("Ã¡", "á")
            .replace("Ã³", "ó")
            .replace("Ã²", "ò")
            .replace("Ãº", "ú")
            .replace("Ã¹", "ù")
            .replace("Ã±", "ñ")
            .replace("Ã§", "ç")
            .replace("Ã®", "î")
            .replace("Ã¯", "ï")
            .replace("Ã´", "ô")
            .replace("Ã¶", "ö")
            .replace("Ã¼", "ü")
            .replace("Ã¤", "ä")
            .replace("Ã«", "ë")
            .replace("Ãª", "ê")
    }

    /**
     * Formats a millisecond timestamp into standard LRC timestamp tag [mm:ss.xx]
     */
    fun formatMsToLrcTimestamp(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = (ms % 1000) / 10
        return String.format(java.util.Locale.US, "[%02d:%02d.%02d]", minutes, seconds, hundredths)
    }

    /**
     * Converts a list of [LyricLine]s into a standard formatted .lrc text string.
     */
    fun formatToLrc(lines: List<LyricLine>): String {
        return lines.sortedBy { it.timestampMs }.joinToString("\n") { line ->
            "${formatMsToLrcTimestamp(line.timestampMs)} ${line.text}"
        }
    }
}

