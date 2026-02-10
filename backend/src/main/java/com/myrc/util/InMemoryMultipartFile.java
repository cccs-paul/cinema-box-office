/*
 * myRC - In-Memory MultipartFile Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 *
 * Description:
 * A simple in-memory implementation of MultipartFile for use during data import.
 * Wraps decoded base64 file content for upload through existing service methods.
 */
package com.myrc.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * In-memory implementation of {@link MultipartFile} for data import operations.
 * Wraps a byte array (decoded from base64) to pass to existing file upload services.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
public class InMemoryMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    /**
     * Construct an in-memory multipart file.
     *
     * @param name the parameter name
     * @param originalFilename the original file name
     * @param contentType the MIME content type
     * @param content the file content bytes
     */
    public InMemoryMultipartFile(String name, String originalFilename,
                                  String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content != null ? content : new byte[0];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("transferTo is not supported for in-memory files");
    }
}
