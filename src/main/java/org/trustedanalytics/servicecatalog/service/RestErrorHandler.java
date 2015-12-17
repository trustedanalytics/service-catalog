/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.servicecatalog.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.CONFLICT;

import org.springframework.security.access.AccessDeniedException;
import org.trustedanalytics.cloud.cc.api.CcOutputBadFormatted;
import org.trustedanalytics.servicecatalog.service.rest.NameAlreadyInUseException;
import org.trustedanalytics.utils.errorhandling.ErrorLogger;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class RestErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestErrorHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public void illegalArgument(Exception e, HttpServletResponse response) throws IOException {
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, BAD_REQUEST, e);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public void unsupportedOperation(Exception e, HttpServletResponse response) throws IOException {
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, NOT_IMPLEMENTED, e);
    }

    @ExceptionHandler(CcOutputBadFormatted.class)
    public void ccOutputBadFormatted(Exception e, HttpServletResponse response) throws IOException {
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void badRequest(Exception e, HttpServletResponse response) throws IOException {
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, BAD_REQUEST, e);
    }

    @ExceptionHandler(NameAlreadyInUseException.class)
    public void nameAlreadyInUse(Exception e, HttpServletResponse response) throws IOException {
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, CONFLICT, e.getMessage(), e);
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public void handleHttpStatusCodeException(HttpStatusCodeException e, HttpServletResponse response) throws IOException {
        String message = extractErrorFromJSON(e.getResponseBodyAsString());
        message = StringUtils.isNotBlank(message) ? message : e.getMessage();
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, e.getStatusCode(), message, e);
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletResponse response) throws Exception {
        org.trustedanalytics.utils.errorhandling.RestErrorHandler defaultErrorHandler = new org.trustedanalytics.utils.errorhandling.RestErrorHandler();
        defaultErrorHandler.handleException(e, response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleFeignException(AccessDeniedException e, HttpServletResponse response) throws IOException {
        ErrorLogger.logAndSendErrorResponse(LOGGER, response, FORBIDDEN, e.getMessage(), e);
    }

    private static String extractErrorFromJSON(String json){
        Map<String, String> map = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(json, new TypeReference<HashMap<String,String>>() {
            });
        } catch (Exception e) {
            LOGGER.error("Error when extracting message from JSON. Continuing...", e);
        }
        return map.get("description");
    }
}
