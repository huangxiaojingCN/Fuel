package com.github.kittinunf.fuel.serialization

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import kotlinx.io.InputStream
import kotlinx.io.Reader
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.serializer

@ImplicitReflectionSerializer
inline fun <reified T : Any> Request.responseObject(
    loader: DeserializationStrategy<T> = T::class.serializer(),
    json: JSON = JSON.plain,
    noinline deserializer: (Request, Response, Result<T, FuelError>) -> Unit
) = response(kotlinxDeserializerOf(loader, json), deserializer)

@ImplicitReflectionSerializer
inline fun <reified T : Any> Request.responseObject(
    deserializer: Handler<T>,
    loader: DeserializationStrategy<T> = T::class.serializer(),
    json: JSON = JSON.plain
) = response(kotlinxDeserializerOf(loader, json), deserializer)

@ImplicitReflectionSerializer
inline fun <reified T : Any> Request.responseObject(
    loader: DeserializationStrategy<T> = T::class.serializer(),
    json: JSON = JSON.plain
) = response(kotlinxDeserializerOf(loader, json))

@ImplicitReflectionSerializer
inline fun <reified T : Any> kotlinxDeserializerOf(loader: DeserializationStrategy<T> = T::class.serializer(), json: JSON = JSON.plain) = object : ResponseDeserializable<T> {
    override fun deserialize(content: String): T? = json.parse(loader, content)
    override fun deserialize(reader: Reader): T? = deserialize(reader.readText())
    override fun deserialize(bytes: ByteArray): T? = deserialize(String(bytes))

    override fun deserialize(inputStream: InputStream): T? {
        inputStream.bufferedReader().use {
            return deserialize(it)
        }
    }
}