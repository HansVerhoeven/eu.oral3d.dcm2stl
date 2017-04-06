package com.dimension4.dcm2stl.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * File storage service interface to allow multiple implementations
 * Created by Polina Petrenko on 03.12.2016.
 */
public interface FileStorageService {
    /**
     * Uploads a file to the configured bucket with the provided key
     * @param inputStream
     * @param contentType
     * @param size
     * @param keyName
     * @return
     * @throws IOException
     */
    String uploadFile(InputStream inputStream, String contentType, long size, String keyName, String subfolder) throws IOException;
    String uploadFileWithManager(InputStream inputStream, String contentType, long size, String keyName, String subfolder, boolean publicAccess);
    String uploadFile(InputStream inputStream, String contentType, long size, String keyName, String subfolder, boolean publicAccess);
    
    String uploadFile(InputStream inputStream, String contentType, long size, String keyName, String subfolder, String ownerId) throws IOException;
    
    /**
     * Downloads the file with the provided keyname from the bucket with the configured bucket name
     * @param keyName
     * @return
     * @throws IOException
     */
    byte[] downloadFile(String keyName) throws IOException;

    /**
     * Download file with URL specified
     * @param url
     * @return
     * @throws IOException
     */
    byte[] downloadUrl(String url) throws IOException;

    /**
     * Checks if URL belongs to configured S3 bucket
     * @param url
     * @return
     */
    boolean isInternalFile(String url);
    
    /**
     * Returns the correct s3 url from the uploaded key
     * @param key
     * @return 
     */
    String getUrlFromKey(String key);
}
