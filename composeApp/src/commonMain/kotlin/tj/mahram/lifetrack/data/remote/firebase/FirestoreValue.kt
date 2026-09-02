package tj.mahram.lifetrack.data.remote.firebase

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * A single record field value. The Realtime Database stores plain JSON, so a
 * value maps straight to a JSON string / number / boolean / null — no typed
 * wrapping (unlike Firestore). This sealed type is the neutral intermediate the
 * per-entity mappers produce and consume.
 */
sealed interface FsValue {
    data class Str(val value: String) : FsValue
    data class Int64(val value: Long) : FsValue
    data class Dbl(val value: Double) : FsValue
    data class Bool(val value: Boolean) : FsValue
    data object Null : FsValue

    fun toJson(): JsonElement = when (this) {
        is Str -> JsonPrimitive(value)
        is Int64 -> JsonPrimitive(value)
        is Dbl -> JsonPrimitive(value)
        is Bool -> JsonPrimitive(value)
        Null -> JsonNull
    }

    companion object {
        fun of(value: String?): FsValue = if (value == null) Null else Str(value)
        fun of(value: Long?): FsValue = if (value == null) Null else Int64(value)
        fun of(value: Double?): FsValue = if (value == null) Null else Dbl(value)
        fun of(value: Boolean?): FsValue = if (value == null) Null else Bool(value)

        /** Decode one plain JSON value (as returned by the Realtime Database). */
        fun fromJson(element: JsonElement): FsValue {
            if (element is JsonNull) return Null
            val primitive = element as? JsonPrimitive ?: return Null
            if (primitive.isString) return Str(primitive.content)
            primitive.booleanOrNull?.let { return Bool(it) }
            primitive.longOrNull?.let { return Int64(it) }
            primitive.doubleOrNull?.let { return Dbl(it) }
            return Str(primitive.content)
        }
    }
}

/** A decoded record: its named field values. Typed accessors coerce as needed. */
class FsFields(private val map: Map<String, FsValue>) {
    val keys: Set<String> get() = map.keys

    fun str(name: String): String? = (map[name] as? FsValue.Str)?.value
    fun long(name: String): Long? = when (val v = map[name]) {
        is FsValue.Int64 -> v.value
        is FsValue.Dbl -> v.value.toLong()
        else -> null
    }
    fun double(name: String): Double? = when (val v = map[name]) {
        is FsValue.Dbl -> v.value
        is FsValue.Int64 -> v.value.toDouble()
        else -> null
    }
    fun bool(name: String): Boolean = when (val v = map[name]) {
        is FsValue.Bool -> v.value
        is FsValue.Int64 -> v.value != 0L
        else -> false
    }

    companion object {
        fun fromJsonObject(obj: JsonObject?): FsFields {
            if (obj == null) return FsFields(emptyMap())
            return FsFields(obj.mapValues { (_, v) -> FsValue.fromJson(v) })
        }
    }
}
