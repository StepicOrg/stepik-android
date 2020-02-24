package org.stepic.droid.jsonHelpers.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.stepik.android.model.code.CodeOptions

class CodeOptionsAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != CodeOptions::class.java) {
            //do not handle it
            return null
        }
        val delegate = gson.getDelegateAdapter(this, type)
        return object : TypeAdapter<T>() {
            override fun read(input: JsonReader): T? {
                val readResult = delegate.read(input)
                val codeOptions = readResult as? CodeOptions
                return if (codeOptions?.executionTimeLimit == 0 && codeOptions.executionMemoryLimit == 0) {
                    // Handling SQL quiz
                    CodeOptions(
                        limits = emptyMap(),
                        executionMemoryLimit = 0,
                        codeTemplates = emptyMap(),
                        executionTimeLimit = 0,
                        samples = emptyList(),
                        isRunUserCodeAllowed = codeOptions.isRunUserCodeAllowed
                    ) as? T
                } else {
                    readResult
                }
            }

            override fun write(out: JsonWriter?, value: T) {
                delegate.write(out, value)
            }
        }
    }

}