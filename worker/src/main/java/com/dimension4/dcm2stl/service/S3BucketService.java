package com.dimension4.dcm2stl.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;
import java.io.FileOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 * Implementation of FileStorageService interface
 * Created by Polina Petrenko on 03.12.2016.
 */
@Service
public class S3BucketService implements FileStorageService {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(S3BucketService.class.getName());
    
    private String accessId = "AKIAILWMXH6TGJTNEUZA";
    
    private String accessSecret = "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";
    
//    @Value("${s3.prefixURL}")
    String urlPrefix = "https://s3-us-west-2.amazonaws.com/com-layeredprints-dcm2stl";

//    @Value("${s3.bucket}")
    String s3BucketName = "com-layeredprints-dcm2stl";

//    @Value("${s3.subfolder}")
//    String subfolder;

    @PostConstruct
    private void init() {
        
    }
    
    @Override
    public String uploadFileWithManager(InputStream inputStream, String contentType, long size, String keyName, String subfolder, boolean publicAccess) {
        Permission perm = Permission.Read;
        AccessControlList fileAccessSetting = new AccessControlList();
        GroupGrantee group = GroupGrantee.AllUsers;
        LOGGER.log(Level.INFO, "Setting AWS S3 file access settings");
        fileAccessSetting.grantPermission(group, perm);
        
        LOGGER.info("Method Start: uploadFile");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);
        
        AWSCredentials credentials = new BasicAWSCredentials(accessId, accessSecret);
        String resultKey = subfolder + "/" + keyName;
        PutObjectRequest por = new PutObjectRequest(
                s3BucketName, resultKey,
                inputStream, metadata).withAccessControlList(fileAccessSetting);
        
        
//        Upload upload = TransferManagerBuilder.defaultTransferManager().upload(por);
        AmazonS3 s3Client = new AmazonS3Client(credentials);
        Upload upload = TransferManagerBuilder.standard().withS3Client(s3Client).build().upload(por);
//        TransferManagerBuilder.standard().setS3Client(s3Client);
//        TransferManager tx = new TransferManager(credentials);
        
//        Upload upload = tx.upload(new PutObjectRequest(
//                s3BucketName, resultKey,
//                inputStream, metadata).withAccessControlList(fileAccessSetting));
        
        ProgressListener uploadPl = new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                System.out.println(upload.getProgress().getPercentTransferred() + "%");
                System.out.println(progressEvent.getBytesTransferred() + " bytes transferred");
                
                switch(progressEvent.getEventType()) {
                    case TRANSFER_PREPARING_EVENT:
                        System.out.println("Preparing transfer");
                        break;
                    case TRANSFER_STARTED_EVENT:
                        System.out.println("Started transfer");
                        break;
                    case TRANSFER_CANCELED_EVENT:
                        System.out.println("Canceled transfer");
                        break;
                    case TRANSFER_PART_COMPLETED_EVENT:
                        System.out.println("Part completed");
                        break;
                    case TRANSFER_PART_FAILED_EVENT:
                        System.out.println("Part failed");
                        break;
                    case TRANSFER_FAILED_EVENT:
                        System.out.println("Failed transfer");
                        break;
                    case TRANSFER_COMPLETED_EVENT:
                        System.out.println("Completed transfer");
                        break;
                    default:
                        System.out.println("Other code received");
                }

            }
        };
        
        upload.addProgressListener(uploadPl);
        
        // turning this off could cause the file to fail at downloading in the slicer worker
        // the task should only be set to ready when this is complete
        try {
            upload.waitForCompletion();
        } catch (AmazonClientException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        String resultUrl = getUrlFromKey(resultKey);
        return resultUrl;
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String contentType, long size, String keyName, String subfolder, boolean publicAccess) {
        Permission perm = Permission.Read;
        AccessControlList fileAccessSetting = new AccessControlList();
        GroupGrantee group = GroupGrantee.AllUsers;
        LOGGER.log(Level.INFO, "Setting AWS S3 file access settings");
        fileAccessSetting.grantPermission(group, perm);
        
        LOGGER.info("Method Start: uploadFile");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);

        AWSCredentials creds = new BasicAWSCredentials(accessId, accessSecret);
        AmazonS3 s3client = new AmazonS3Client(creds);
        String resultKey = subfolder + "/" + keyName;
        
        if(publicAccess) {
            s3client.putObject(new PutObjectRequest(
                s3BucketName, resultKey,
                inputStream, metadata).withAccessControlList(fileAccessSetting));
        } else {
            s3client.putObject(new PutObjectRequest(
                s3BucketName, resultKey,
                inputStream, metadata));
        }

        LOGGER.info("Method End: uploadFile");
        String resultUrl = getUrlFromKey(resultKey);
        return resultUrl;
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String contentType, long size, String keyName, String subfolder, String ownerId) {
        Permission perm = Permission.Read;
        AccessControlList fileAccessSetting = new AccessControlList();
        LOGGER.log(Level.INFO, "Set AWS S3 file access to [{0}] for user [{1}]", new Object[]{perm.toString(), ownerId});
        fileAccessSetting.grantPermission(new CanonicalGrantee(ownerId), perm);
        
        LOGGER.info("Method Start: uploadFile");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);

        AWSCredentials creds = new BasicAWSCredentials(accessId, accessSecret);
        AmazonS3 s3client = new AmazonS3Client(creds);
        String resultKey = subfolder + "/" + keyName;
        s3client.putObject(new PutObjectRequest(
                s3BucketName, resultKey,
                inputStream, metadata).withAccessControlList(fileAccessSetting));

        LOGGER.info("Method End: uploadFile");
        String resultUrl = getUrlFromKey(resultKey);
        return resultUrl;
    }
    
    
    @Override
    public String uploadFile(InputStream inputStream, String contentType, long size, String keyName, String subfolder) {

        LOGGER.info("Method Start: uploadFile");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);

        AWSCredentials creds = new BasicAWSCredentials(accessId, accessSecret);
        AmazonS3 s3client = new AmazonS3Client(creds);
        String resultKey = subfolder + "/" + keyName;
        s3client.putObject(new PutObjectRequest(
                s3BucketName, resultKey,
                inputStream, metadata));

        LOGGER.info("Method End: uploadFile");
        String resultUrl = getUrlFromKey(resultKey);
        return resultUrl;
    }

    @Override
    public byte[] downloadFile(String keyName) throws IOException {
        LOGGER.info("Method Start: downloadFile");
        LOGGER.log(Level.INFO, "downloading file with key {0}", keyName);

//        AmazonS3 s3client = new AmazonS3Client();
        AWSCredentials creds = new BasicAWSCredentials(accessId, accessSecret);
        AmazonS3 s3client = new AmazonS3Client(creds);
        S3Object object = s3client.getObject(
                new GetObjectRequest(s3BucketName, keyName));

        InputStream objectData = object.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectData);
        objectData.close();

        LOGGER.info("Method End: downloadFile");
        return bytes;
    }
    
    private byte[] downloadFile(String keyName, String bucketName) throws IOException {
        LOGGER.info("Method Start: downloadFile");
        LOGGER.log(Level.INFO, "downloading file with key {0} and bucket {1}", new Object[]{keyName, bucketName});

//        AmazonS3 s3client = new AmazonS3Client();
        AWSCredentials creds = new BasicAWSCredentials(accessId, accessSecret);
        AmazonS3 s3client = new AmazonS3Client(creds);
        S3Object object = s3client.getObject(
                new GetObjectRequest(bucketName, keyName));

        InputStream objectData = object.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectData);
        objectData.close();

        LOGGER.info("Method End: downloadFile");
        return bytes;
    }
 
    @Override
    public byte[] downloadUrl(String url) throws IOException {
        if(isInternalFile(url)) {
            String keyName = getKeyFromUrl(url);
            return downloadFile(keyName);
        } else {
            String bucketName = getBucketFromUrl(url);
            String keyName = getKeyFromUrl(url, bucketName);
            return downloadFile(keyName, bucketName);
        }
    }

    @Override
    public boolean isInternalFile(String url) {
        if(StringUtils.isBlank(url)) {
            return false;
        }
        return url.startsWith(urlPrefix);
    }

    /**
     * Extracts S3 bucket key from provided URL
     * @param url
     * @return
     */
    private String getKeyFromUrl(String url) {
        if(StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Cannot get key from empty url");
        }
        if(!url.startsWith(urlPrefix)) {
            if(StringUtils.endsWith(url, ".dcm")) {
                LOGGER.log(Level.INFO, "substringAfterLast {0}", StringUtils.substringAfterLast(url, "/"));
                LOGGER.log(Level.INFO, "substringsBetween {0}", StringUtils.substringsBetween(url, "/", ".dcm"));
                LOGGER.log(Level.INFO, "substringAfterLast {0}", StringUtils.substringAfterLast(url, "/"));
                String[] splitted = url.split("/");
                for(int i = 0; i < splitted.length; i++) {
                    LOGGER.log(Level.INFO, "at {0} is {1}", new Object[]{i, splitted[i]});
                }
                return splitted[splitted.length-1];
            } else {
                throw new IllegalArgumentException("url doesnt contain a DICOM file");
            }
        }
        String suffix = url.substring(urlPrefix.length());
        LOGGER.log(Level.INFO, "suffix is {0}", suffix);
        if(suffix.startsWith("/")) {
            return suffix.substring(1);
        } else {
            return suffix;
        }
    }
    
    private String getKeyFromUrl(String url, String bucketName) {
        if(StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Cannot get key from empty url");
        }
        String keyName = StringUtils.substringAfter(url, bucketName + "/");
        LOGGER.log(Level.INFO, "found keyName {0}", keyName);
        return keyName;
    }
    
    private String getBucketFromUrl(String url) {
        // the bucket is what comes after the last slash after .com
        String afterDotCom = StringUtils.substringAfterLast(url, ".com/");
        LOGGER.log(Level.INFO, "afterDotCom {0}", afterDotCom);
        String[] splittedAfterDotCom = afterDotCom.split("/");
        String resultBucket = splittedAfterDotCom[0];
        LOGGER.log(Level.INFO, "resultBucket {0}", resultBucket);
//        // remove the key so the bucket is left at the end
//        String urlWithBucket = StringUtils.substringBefore(url, key);
//        // split it to get the last part
//        String[] splitted = urlWithBucket.split("/");
//        // the last index will contain the bucket
//        String bucketString = splitted[splitted.length-1];
        return resultBucket;
    }
    
    @Override
    public String getUrlFromKey(String key) {
        if(StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Cannot get url from empty key");
        }
        return urlPrefix + "/" + key;
    }
    
}
/*
@Override
    public String uploadFileWithManager(InputStream inputStream, String contentType, long size, String keyName, String subfolder, boolean publicAccess) {
        Permission perm = Permission.Read;
        AccessControlList fileAccessSetting = new AccessControlList();
        GroupGrantee group = GroupGrantee.AllUsers;
        LOGGER.log(Level.INFO, "Setting AWS S3 file access settings");
        fileAccessSetting.grantPermission(group, perm);
        
        LOGGER.info("Method Start: uploadFile");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);
        
        AWSCredentials credentials = new BasicAWSCredentials(accessId, accessSecret);
        TransferManager tx = new TransferManager(credentials);
        
        String resultKey = subfolder + "/" + keyName;
        Upload upload = tx.upload(new PutObjectRequest(
                s3BucketName, resultKey,
                inputStream, metadata).withAccessControlList(fileAccessSetting));
        
        ProgressListener uploadPl = new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                System.out.println(upload.getProgress().getPercentTransferred() + "%");
                System.out.println(progressEvent.getBytesTransferred() + " bytes transferred");
                if (progressEvent.getEventType() == ProgressEventType.HTTP_REQUEST_COMPLETED_EVENT) {
                    System.out.println("Upload complete!!!");
//                    tx.shutdownNow();
                }
            }
        };
        
        upload.addProgressListener(uploadPl);
        String resultUrl = getUrlFromKey(resultKey);
        return resultUrl;
    }
*/