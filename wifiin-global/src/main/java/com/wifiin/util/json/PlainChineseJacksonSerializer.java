package com.wifiin.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PlainChineseJacksonSerializer extends JsonSerializer<String>{

	@Override
	public void serialize(String value, JsonGenerator generator, SerializerProvider provider) throws IOException,JsonProcessingException {
		generator.writeString(value);
	}

}
