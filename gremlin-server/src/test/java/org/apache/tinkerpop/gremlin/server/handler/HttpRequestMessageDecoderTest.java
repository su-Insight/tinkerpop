/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.apache.tinkerpop.gremlin.util.MessageSerializerV4;
import org.apache.tinkerpop.gremlin.util.TokensV4;
import org.apache.tinkerpop.gremlin.util.message.RequestMessageV4;
import org.apache.tinkerpop.gremlin.util.ser.GraphBinaryMessageSerializerV4;
import org.apache.tinkerpop.gremlin.util.ser.GraphSONMessageSerializerV4;
import org.apache.tinkerpop.gremlin.util.ser.SerTokensV4;
import org.apache.tinkerpop.gremlin.util.ser.SerializationException;
import org.apache.tinkerpop.gremlin.util.ser.SerializersV4;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HttpRequestMessageDecoderTest {

    private final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    private final GraphBinaryMessageSerializerV4 graphBinarySerializer = new GraphBinaryMessageSerializerV4();
    public final GraphSONMessageSerializerV4 graphSONSerializer = new GraphSONMessageSerializerV4();

    private final static Map<String, MessageSerializerV4<?>> serializers = new HashMap<>();
    static {
        serializers.put(SerializersV4.GRAPHSON_V4_UNTYPED.getValue(), SerializersV4.GRAPHSON_V4_UNTYPED.simpleInstance());
        serializers.put("application/json", SerializersV4.GRAPHSON_V4_UNTYPED.simpleInstance());
        serializers.put(SerializersV4.GRAPHSON_V4.getValue(), SerializersV4.GRAPHSON_V4.simpleInstance());
        serializers.put(SerializersV4.GRAPHBINARY_V4.getValue(), SerializersV4.GRAPHBINARY_V4.simpleInstance());
    }

    @Test
    public void shouldFailWhenIncorrectSerializerUsed() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final RequestMessageV4 request = RequestMessageV4.build("g.V()").create();

        final ByteBuf buffer = graphSONSerializer.serializeRequestAsBinary(request, allocator);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_GRAPHBINARY_V4);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "some uri",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        assertNull(testChannel.readInbound());

        ByteBuf out = testChannel.readOutbound();
        assertTrue(out.toString(CharsetUtil.UTF_8).contains("Unable to deserialize request using"));
    }

    @Test
    public void shouldCorrectlyDeserializeRequestMessage() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final RequestMessageV4 request = RequestMessageV4.build("g.V()").addLanguage("gremlin-lang").create();

        final ByteBuf buffer = graphBinarySerializer.serializeRequestAsBinary(request, allocator);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_GRAPHBINARY_V4);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "some uri",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        final RequestMessageV4 decodedRequestMessage = testChannel.readInbound();
        assertThat(request.getFields(), samePropertyValuesAs(decodedRequestMessage.getFields()));
        assertEquals(request.getGremlin(), decodedRequestMessage.getGremlin());
    }

    @Test
    public void shouldCorrectlyDeserializeGremlinFromPostRequest() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final String gremlin = "g.V().hasLabel('person')";
        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{ \"gremlin\": \"" + gremlin +
                        "\", \"language\":  \"gremlin-groovy\"}",
                CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "some uri",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        final RequestMessageV4 decodedRequestMessage = testChannel.readInbound();
        assertEquals(gremlin, decodedRequestMessage.getGremlin());
        assertEquals("gremlin-groovy", decodedRequestMessage.getField(TokensV4.ARGS_LANGUAGE));
    }

    @Test
    public void shouldCorrectlyDeserializeGremlinFromPostRequestWithAllScriptFieldsSet() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final String gremlin = "g.V(x)";
        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{ \"gremlin\": \"" + gremlin +
                        "\", \"bindings\":{\"x\":\"2\"}" +
                        ", \"language\":  \"gremlin-groovy\"}",
                CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        final RequestMessageV4 decodedRequestMessage = testChannel.readInbound();
        assertEquals(gremlin, decodedRequestMessage.getGremlin());
        assertEquals("gremlin-groovy", decodedRequestMessage.getField(TokensV4.ARGS_LANGUAGE));
        assertEquals("2", ((Map)decodedRequestMessage.getField(TokensV4.ARGS_BINDINGS)).get("x"));
    }

    @Test
    public void shouldErrorOnBadRequestWithMalformedJson() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final String gremlin = "g.V(x)";
        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{ \"gremlin\": \"" + gremlin +
                        "\" \"language\":  \"gremlin-groovy\"}",
                CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        assertNull(testChannel.readInbound());

        ByteBuf out = testChannel.readOutbound();
        assertTrue(out.toString(CharsetUtil.UTF_8).contains("body could not be parsed"));
    }

    @Test
    public void shouldIgnoreInvalidRequestMessageParameter() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final String gremlin = "g.V(x)";
        final ByteBuf buffer = allocator.buffer();
        // language contains a typo here as lnguage
        buffer.writeCharSequence("{ \"gremlin\": \"" + gremlin +
                        "\", \"lnguage\":  \"abc\"}",
                CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        final RequestMessageV4 decodedRequestMessage = testChannel.readInbound();
        assertNotEquals("abc", decodedRequestMessage.getField(TokensV4.ARGS_LANGUAGE));
    }

    @Test
    public void shouldErrorOnBadRequestWithNoParameter() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{ }", CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        assertNull(testChannel.readInbound());

        ByteBuf out = testChannel.readOutbound();
        assertTrue(out.toString(CharsetUtil.UTF_8).contains("no gremlin script supplied"));
    }

    @Test
    public void shouldAttemptToParseRequestWithNonsenseContentType() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{\"gremlin\":\"g.V()\"}", CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, "some-nonexistent-serializer");

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        RequestMessageV4 decodedRequest = testChannel.readInbound();
        assertNotNull(decodedRequest);
    }

    @Test
    public void shouldErrorWithNonexistentAcceptHeader() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{\"gremlin\":\"g.V()\"}", CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.ACCEPT, "some-nonexistent-serializer");

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        assertNull(testChannel.readInbound());

        ByteBuf response = testChannel.readOutbound();
        assertTrue(response.toString(CharsetUtil.UTF_8).contains("no serializer for requested Accept header"));
    }

    @Test
    public void shouldNotAddInvalidFieldToRequestMessage() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{\"gremlin\":\"g.V()\",\"nonfield\":\"shouldntgetadded\"}", CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        RequestMessageV4 decodedRequest = testChannel.readInbound();
        assertNull(decodedRequest.getField("nonfield"));
        assertEquals("g.V()", decodedRequest.getGremlin());
    }

    @Test
    public void shouldAddValidFieldsToRequestMessage() throws SerializationException {
        final HttpRequestMessageDecoder requestMessageDecoder = new HttpRequestMessageDecoder(serializers);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), requestMessageDecoder);

        final UUID rid = UUID.randomUUID();
        final ByteBuf buffer = allocator.buffer();
        buffer.writeCharSequence("{\"gremlin\":\"g.V().limit(2)\",\"batchSize\":\"10\",\"language\":\"gremlin-lang\"," +
                "\"g\":\"gmodern\",\"bindings\":{\"x\":\"1\"},\"timeoutMs\":\"12\"," +
                "\"materializeProperties\":\"" + TokensV4.MATERIALIZE_PROPERTIES_TOKENS + "\"}", CharsetUtil.UTF_8);

        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, SerTokensV4.MIME_JSON);

        final FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "",
                buffer, headers, new DefaultHttpHeaders());

        testChannel.writeInbound(httpRequest);
        testChannel.finish();

        RequestMessageV4 decodedRequest = testChannel.readInbound();
        assertEquals("g.V().limit(2)", decodedRequest.getGremlin());
        assertEquals(10, (int) decodedRequest.getField(TokensV4.ARGS_BATCH_SIZE));
        assertEquals("gremlin-lang", decodedRequest.getField(TokensV4.ARGS_LANGUAGE));
        assertEquals("gmodern", decodedRequest.getField(TokensV4.ARGS_G));
        assertEquals("1", ((Map) decodedRequest.getField(TokensV4.ARGS_BINDINGS)).get("x"));
        assertEquals(1, ((Map) decodedRequest.getField(TokensV4.ARGS_BINDINGS)).size());
        assertEquals(12, (long) decodedRequest.getField(TokensV4.TIMEOUT_MS));
        assertEquals(TokensV4.MATERIALIZE_PROPERTIES_TOKENS, decodedRequest.getField(TokensV4.ARGS_MATERIALIZE_PROPERTIES));
    }
}
