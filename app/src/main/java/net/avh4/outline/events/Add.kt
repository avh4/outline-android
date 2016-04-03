package net.avh4.outline.events

import com.fasterxml.jackson.core.JsonGenerator
import net.avh4.Event
import net.avh4.json.FromJsonValue
import net.avh4.outline.Outline
import net.avh4.outline.OutlineNodeId
import java.io.IOException

class Add(private val parent: OutlineNodeId, private val id: OutlineNodeId, private val value: String)
: Event<Outline> {

    override fun execute(outline: Outline): Outline {
        return outline.addChild(parent, id, value)
    }

    @Throws(IOException::class)
    override fun toJson(generator: JsonGenerator) {
        generator.writeStartObject()
        generator.writeStringField("parent", parent.toString())
        generator.writeStringField("id", id.toString())
        generator.writeStringField("value", value)
        generator.writeEndObject()
    }

    override fun eventType(): String {
        return eventType
    }

    companion object {
        val fromJson: FromJsonValue<Add> = FromJsonValue { context ->
            context.getObject { context ->
                val parent = context.getString("parent")
                val id = context.getString("id")
                val value = context.getString("value")
                Add(OutlineNodeId(parent), OutlineNodeId(id), value)
            }
        }
        val eventType = "add"
    }
}
