package com.seanshubin.condorcet.deploy.domain

import java.io.*
import java.nio.charset.Charset

object IoUtil {
  fun feedInputStreamToOutputStream(inputStream: InputStream, outputStream: OutputStream) {
    var byte = inputStream.read()
    while (byte != -1) {
      outputStream.write(byte)
      byte = inputStream.read()
    }
  }

  fun feedReaderToWriter(reader: Reader, writer: Writer) {
    var char = reader.read()
    while (char != -1) {
      writer.write(char)
      char = reader.read()
    }
  }

  fun inputStreamToBytes(inputStream: InputStream): ByteArray {
    val outputStream = ByteArrayOutputStream()
    feedInputStreamToOutputStream(inputStream, outputStream)
    return outputStream.toByteArray()
  }

  fun readerToString(reader: Reader): String {
    val writer = StringWriter()
    feedReaderToWriter(reader, writer)
    return writer.buffer.toString()
  }

  fun bytesToInputStream(bytes: ByteArray): InputStream = ByteArrayInputStream(bytes)

  fun stringToInputStream(s: String, charset: Charset): InputStream = bytesToInputStream(s.toByteArray(charset))

  fun inputStreamToString(inputStream: InputStream, charset: Charset): String =
      String(inputStreamToBytes(inputStream), charset)

  fun stringToReader(s: String): Reader = StringReader(s)

  fun stringToOutputStream(s: String, charset: Charset, outputStream: OutputStream) =
      outputStream.write(stringToBytes(s, charset))

  fun bytesToString(bytes: ByteArray, charset: Charset): String = String(bytes, charset)

  fun stringToBytes(s: String, charset: Charset): ByteArray = s.toByteArray(charset)

  fun bytesToOutputStream(bytes: ByteArray, outputStream: OutputStream) {
    val inputStream = bytesToInputStream(bytes)
    feedInputStreamToOutputStream(inputStream, outputStream)
  }
}
