package com.example.encryptMsg.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

// SpringBoot's JSON parser "Jackson" automatically converts payload to immutable String.
/*  String literals are not cleared as readily by Java's garbage collector;
    sensitive payloads should thus be parsed into char-arrays ASAP.
 */
public class CharArrDeserialization extends JsonDeserializer<char[]> {
    @Override
    public char[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        char[] textBuffer = p.getTextCharacters();
        int textLength = p.getTextLength();

        char[] toReturn = new char[textLength];
        System.arraycopy(
                textBuffer, p.getTextOffset(),
                toReturn, 0,
                textLength
        );
        return toReturn;
    }
}
