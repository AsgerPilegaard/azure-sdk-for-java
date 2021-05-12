// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 * These tests will not run in LIVE or RECORD as they cannot get the content url.
 */
public class DownloadContentTests extends CallingServerTestBase {
    private static final String METADATA_URL = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/acsmetadata";
    private static final String VIDEO_URL = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/video";
    private static final String CONTENT_URL_404 = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141d/content/acsmetadata";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadMetadata(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "downloadMetadata");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            conversationClient.downloadTo(baos, new URI(METADATA_URL), null, null);
            String metadata = baos.toString(StandardCharsets.UTF_8);
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadMetadataStreaming(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "downloadMetadataStreaming");

        try {
            Flux<ByteBuffer> stream = conversationClient.downloadStreaming(new URI(METADATA_URL));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            stream.subscribe(byteBuffer -> {
                try {
                    baos.write(byteBuffer.array());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            String metadata = baos.toString(StandardCharsets.UTF_8);
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadVideo(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "downloadVideo");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Response<Void> response = conversationClient
                .downloadToWithResponse(baos, new URI(VIDEO_URL), null, null, null);
            assertThat(response, is(notNullValue()));
            assertThat(response.getHeaders().getValue("Content-Type"), is(equalTo("application/octet-stream")));
            assertThat(Integer.parseInt(response.getHeaders().getValue("Content-Length")), is(equalTo(baos.size())));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadContent404(HttpClient httpClient) {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationClient conversationClient = setupClient(builder, "downloadContent404");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommunicationErrorException ex = assertThrows(CommunicationErrorException.class,
            () -> conversationClient
                .downloadTo(baos, new URI(CONTENT_URL_404), null, null));
        assertThat(ex.getResponse().getStatusCode(), is(equalTo(404)));
    }

    private ConversationClient setupClient(ConversationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected ConversationClientBuilder addLoggingPolicy(ConversationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
