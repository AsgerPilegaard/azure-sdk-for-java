// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.ConversationsImpl;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.implementation.converters.InviteParticipantsRequestConverter;
import com.azure.communication.callingserver.implementation.converters.JoinCallRequestConverter;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingRequest;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.JoinCallResponse;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Async client that supports server conversation operations.
 */
@ServiceClient(builder = CallClientBuilder.class, isAsync = true)
public final class ConversationAsyncClient {
    private final ConversationsImpl conversationsClient;
    private final ClientLogger logger = new ClientLogger(ConversationAsyncClient.class);
    private final ContentDownloader contentDownloader;

    ConversationAsyncClient(AzureCommunicationCallingServerServiceImpl conversationServiceClient) {
        conversationsClient = conversationServiceClient.getConversations();
        contentDownloader = new ContentDownloader(
            conversationServiceClient.getEndpoint(),
            conversationServiceClient.getHttpPipeline(),
            logger);
    }

    /**
     * Join a Call
     *
     * @param conversationId The conversation id.
     * @param source to Join Call.
     * @param joinCallOptions join call options.
     * @return response for a successful joinCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JoinCallResponse> joinCall(String conversationId, CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");

            return this.conversationsClient.joinCallAsync(conversationId, JoinCallRequestConverter.convert(source, joinCallOptions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param conversationId The conversation id.
     * @param source to Join Call.
     * @param joinCallOptions join call options.
     * @return response for a successful joincall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JoinCallResponse>>joinCallWithResponse(String conversationId, CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        return joinCallWithResponse(conversationId, source, joinCallOptions, null);
    }

    Mono<Response<JoinCallResponse>> joinCallWithResponse(
        String conversationId,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions,
        Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(source, "'request' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.joinCallWithResponseAsync(conversationId,
                    JoinCallRequestConverter.convert(source, joinCallOptions));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Invite Participats to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param alternateCallerId alternateCallerId of Invited participant.
     * @param operationContext operationContext.
     * @param callBackUri callBackUri to get notifications.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(String conversationId, CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");

            InviteParticipantsRequest request = InviteParticipantsRequestConverter.convert(participant, alternateCallerId, operationContext, callBackUri);

            return this.conversationsClient.inviteParticipantsAsync(conversationId, request);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Invite Participats to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param alternateCallerId alternateCallerId of Invited participant.
     * @param operationContext operationContext.
     * @param callBackUri callBackUri to get notifications.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(String conversationId, CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri) {
        return addParticipantWithResponse(conversationId, participant, alternateCallerId, operationContext, callBackUri, null);
    }

    Mono<Response<Void>> addParticipantWithResponse(
        String conversationId,
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext,
        String callBackUri,
        Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request = InviteParticipantsRequestConverter.convert(participant, alternateCallerId, operationContext, callBackUri);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.inviteParticipantsWithResponseAsync(conversationId, request);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

       /**
     * Remove participant from the Conversation.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String conversationId, String participantId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");

            return this.conversationsClient.removeParticipantAsync(conversationId, participantId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participant from the Conversation.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String conversationId, String participantId) {
        return removeParticipantWithResponse(conversationId, participantId, null);
    }

    /**
     * Remove participant from the Conversation.
     */
    Mono<Response<Void>> removeParticipantWithResponse(String conversationId, String participantId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.removeParticipantWithResponseAsync(conversationId, participantId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StartCallRecordingResponse> startRecording(String conversationId, String recordingStateCallbackUri) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }

            StartCallRecordingRequest request = new StartCallRecordingRequest().setRecordingStateCallbackUri(recordingStateCallbackUri);
            return this.conversationsClient.startRecordingAsync(conversationId, request)
                    .flatMap((StartCallRecordingResponse response) -> {
                        return Mono.just(response);
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StartCallRecordingResponse>> startRecordingWithResponse(String conversationId, String recordingStateCallbackUri) {
        return startRecordingWithResponse(conversationId, recordingStateCallbackUri, null);
    }

    Mono<Response<StartCallRecordingResponse>> startRecordingWithResponse(String conversationId, String recordingStateCallbackUri, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(new URI(recordingStateCallbackUri).isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' cannot be non absolute Uri"));
            }
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                StartCallRecordingRequest request = new StartCallRecordingRequest().setRecordingStateCallbackUri(recordingStateCallbackUri);
                return this.conversationsClient.startRecordingWithResponseAsync(conversationId, request)
                        .flatMap((Response<StartCallRecordingResponse> response) -> {
                            return Mono.just(new SimpleResponse<>(response, response.getValue()));
                        });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        } catch (URISyntaxException ex) {
            return monoError(logger, new RuntimeException(ex.getMessage()));
        }
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.stopRecordingAsync(conversationId, recordingId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopRecordingWithResponse(String conversationId, String recordingId) {
        return stopRecordingWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.stopRecordingWithResponseAsync(conversationId, recordingId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> pauseRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.pauseRecordingAsync(conversationId, recordingId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> pauseRecordingWithResponse(String conversationId, String recordingId) {
        return pauseRecordingWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.pauseRecordingWithResponseAsync(conversationId, recordingId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resumeRecording(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.resumeRecordingAsync(conversationId, recordingId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeRecordingWithResponse(String conversationId, String recordingId) {
        return resumeRecordingWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String conversationId, String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.resumeRecordingWithResponseAsync(conversationId, recordingId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallRecordingStateResponse> getRecordingState(String conversationId, String recordingId) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return this.conversationsClient.recordingStateAsync(conversationId, recordingId)
                    .flatMap((CallRecordingStateResponse response) -> {
                        return Mono.just(response);
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallRecordingStateResponse>> getRecordingStateWithResponse(String conversationId,
            String recordingId) {
        return getRecordingStateWithResponse(conversationId, recordingId, null);
    }

    Mono<Response<CallRecordingStateResponse>> getRecordingStateWithResponse(String conversationId,
            String recordingId, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(recordingId, "'recordingId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.recordingStateWithResponseAsync(conversationId, recordingId)
                        .flatMap((Response<CallRecordingStateResponse> response) -> {
                            return Mono.just(new SimpleResponse<>(response, response.getValue()));
                        });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The uri of the audio file .
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResponse> playAudio(String conversationId, String audioFileUri, String audioFileId, String callbackUri, String operationContext) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
            PlayAudioRequest playAudioRequest = new PlayAudioRequest().
                setAudioFileUri(audioFileUri).setLoop(false).setAudioFileId(audioFileId).setCallbackUri(callbackUri).setOperationContext(operationContext);
            return playAudio(conversationId, playAudioRequest);

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<PlayAudioResponse> playAudio(String conversationId, PlayAudioRequest request) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.conversationsClient.playAudioAsync(conversationId, request).flatMap(
                (PlayAudioResponse response) -> {
                    return Mono.just(response);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The uri of the audio file .
     * @param audioFileId Tne id for the media in the AudioFileUri, using which we cache the media resource.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The operation context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResponse>> playAudioWithResponse(String conversationId, String audioFileUri, String audioFileId, String callbackUri, String operationContext) {

        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = new PlayAudioRequest().
            setAudioFileUri(audioFileUri).setLoop(false).setAudioFileId(audioFileId).setCallbackUri(callbackUri).setOperationContext(operationContext);
        return playAudioWithResponse(conversationId, playAudioRequest, null);
    }

    Mono<Response<PlayAudioResponse>> playAudioWithResponse(String conversationId, PlayAudioRequest request, Context context) {
        try {
            Objects.requireNonNull(conversationId, "'conversationId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.conversationsClient.playAudioWithResponseAsync(conversationId, request).flatMap((
                        Response<PlayAudioResponse> response) -> {
                    return Mono.just(new SimpleResponse<>(response, response.getValue()));
                });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param endpoint - URL where the content is located.
     * @return A {@link Mono} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Flux<ByteBuffer>> downloadStreaming(URI endpoint) {
        return downloadStreamingWithResponse(endpoint, null).map(Response::getValue);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param endpoint - URL where the content is located.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Mono} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Flux<ByteBuffer>> downloadStreaming(URI endpoint, HttpRange httpRange) {
        return downloadStreamingWithResponse(endpoint, httpRange).map(Response::getValue);
    }

    /**
     * Download the recording content, (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}.
     * @param endpoint - URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Mono} object containing a {@link Response} with the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> downloadStreamingWithResponse(URI endpoint, HttpRange range) {
        return downloadStreamingWithResponse(endpoint, range, null);
    }

    Mono<Response<Flux<ByteBuffer>>> downloadStreamingWithResponse(URI endpoint, HttpRange range, Context context) {
        try {
            Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
            return contentDownloader.downloadStreamingWithResponse(endpoint, range, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the recording content (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}
     * and written into the {@code stream}.
     * @param stream - Stream to write the content to.
     * @param endpoint - Location of the recording's content.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param parallelDownloadOptions - An optional {@link ParallelDownloadOptions} object to modify how the
     *                               parallel download will work.
     * @return An empty {@link Mono} object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Void> downloadTo(
        OutputStream stream,
        URI endpoint,
        HttpRange range,
        ParallelDownloadOptions parallelDownloadOptions) {

        return Mono.defer(() -> {
            downloadToWithResponse(stream, endpoint, range, parallelDownloadOptions).block();
            return Mono.empty();
        });
    }

    /**
     * Download the recording content (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}
     * and written into the {@code stream}.
     * @param stream - Stream to write the content to.
     * @param endpoint - Location of the recording's content.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param parallelDownloadOptions - An optional {@link ParallelDownloadOptions} object to modify how the
     *                               parallel download will work.
     * @return A {@link Mono} object containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> downloadToWithResponse(
        OutputStream stream,
        URI endpoint,
        HttpRange range,
        ParallelDownloadOptions parallelDownloadOptions) {

        return downloadToWithResponse(stream, endpoint, range, parallelDownloadOptions, null);
    }

    Mono<Response<Void>> downloadToWithResponse(
        OutputStream stream,
        URI endpoint,
        HttpRange range,
        ParallelDownloadOptions parallelDownloadOptions,
        Context context) {
        HttpRange finalRange = range == null ? new HttpRange(0) : range;
        ParallelDownloadOptions finalParallelDownloadOptions =
            parallelDownloadOptions == null
                ? new ParallelDownloadOptions()
                : parallelDownloadOptions;

        return contentDownloader.downloadToStream(stream, endpoint, finalRange, finalParallelDownloadOptions, context);
    }

    /**
     * Download the recording content (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}
     * and stored into the file in {@code path}.
     * @param path - File path to store file to.
     * @param endpoint - Location of the recording's content.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param parallelDownloadOptions - An optional {@link ParallelDownloadOptions} object to modify how the
     *                               parallel download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @return An empty {@link Mono} object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> downloadTo(
        Path path,
        URI endpoint,
        HttpRange range,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite) {
        return downloadToWithResponse(path, endpoint, range, parallelDownloadOptions, overwrite)
            .map(Response::getValue);
    }

    /**
     * Download the recording content (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}
     * and stored into the file in {@code path}.
     * @param path - File path to store file to.
     * @param endpoint - Location of the recording's content.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param parallelDownloadOptions - An optional {@link ParallelDownloadOptions} object to modify how the
     *                               parallel download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @return A {@link Mono} object containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> downloadToWithResponse(
        Path path,
        URI endpoint,
        HttpRange range,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite) {
        return downloadToWithResponse(path, endpoint, range, parallelDownloadOptions, overwrite, null);
    }

    Mono<Response<Void>> downloadToWithResponse(
        Path path,
        URI endpoint,
        HttpRange range,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite,
        Context context) {
        HttpRange finalRange = range == null ? new HttpRange(0) : range;
        ParallelDownloadOptions finalParallelDownloadOptions =
            parallelDownloadOptions == null
                ? new ParallelDownloadOptions()
                : parallelDownloadOptions;
        Set<OpenOption> openOptions = new HashSet<>();

        if (overwrite) {
            openOptions.add(StandardOpenOption.CREATE);
        } else {
            openOptions.add(StandardOpenOption.CREATE_NEW);
        }
        openOptions.add(StandardOpenOption.WRITE);

        try {
            AsynchronousFileChannel file = AsynchronousFileChannel.open(path, openOptions, null);
            return Mono.just(file).flatMap(
                c -> contentDownloader.downloadToFile(c, endpoint, finalRange, finalParallelDownloadOptions, context))
                .doFinally(signalType -> contentDownloader.downloadToFileCleanup(file, path, signalType));
        } catch (IOException ex) {
            return monoError(logger, new RuntimeException(ex));
        }
    }
}
