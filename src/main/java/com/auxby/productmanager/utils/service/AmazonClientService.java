package com.auxby.productmanager.utils.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmazonClientService {
    @Value("${aws.bucket}")
    private String bucket;
    @Value("${aws.endpoint}")
    private String endpoint;

    private final AmazonS3 amazonS3;

    public String uploadPhoto(File photo, String ownerUuid, Integer offerId) {
        String fileName = String.format("%s/%s/%s", ownerUuid, offerId, "img-" + System.currentTimeMillis());
        String fileUrl = endpoint + "/" + bucket + "/" + fileName;
        uploadFileToS3Bucket(fileName, photo);
        photo.delete();

        return fileUrl;
    }

    public void deleteOfferResources(String userUuid, Integer offerId) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucket)
                .withPrefix(userUuid + "/" + offerId);
        List<S3ObjectSummary> objectSummaries = amazonS3.listObjects(listObjectsRequest)
                .getObjectSummaries();
        if (!objectSummaries.isEmpty()) {
            ArrayList<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
            objectSummaries.forEach(
                    s3ObjectSummary -> keys.add(new DeleteObjectsRequest.KeyVersion(s3ObjectSummary.getKey()))
            );
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucket)
                    .withKeys(keys)
                    .withQuiet(false);
            amazonS3.deleteObjects(request);
        }
    }

    public File convertToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(convFile)) {
            fileOutputStream.write(multipartFile.getBytes());
        }

        return convFile;
    }

    private void uploadFileToS3Bucket(String fileName, File file) {
        var uploadRequest = new PutObjectRequest(bucket, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(uploadRequest);
    }
}
