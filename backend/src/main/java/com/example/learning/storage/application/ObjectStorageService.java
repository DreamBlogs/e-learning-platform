package com.example.learning.storage.application;

import java.io.InputStream;

public interface ObjectStorageService {

    void putObject(String objectKey, InputStream inputStream, long size, String contentType);

    InputStream getObject(String objectKey);

    void removeObject(String objectKey);
}
