package com.auxby.productmanager.utils.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmazonClientServiceTest {

    @InjectMocks
    AmazonClientService amazonClientService;
    @Mock
    private AmazonS3 amazonS3;

    @Test
    void uploadPhoto() {
        java.io.File file = mock(File.class);
        amazonClientService.uploadPhoto(file, "uuid-test", 1);

        ArgumentCaptor<PutObjectRequest> putObjArg = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3, times(1)).putObject(putObjArg.capture());
        assertTrue(putObjArg.getValue().getKey().contains("uuid-test/1/"));
    }

    @Test
    void deleteOfferResources() {
        var listingMock = mock(ObjectListing.class);
        var s3ObjectMock = mock(S3ObjectSummary.class);
        when(listingMock.getObjectSummaries())
                .thenReturn(List.of(s3ObjectMock));
        when(amazonS3.listObjects(any(ListObjectsRequest.class)))
                .thenReturn(listingMock);

        amazonClientService.deleteOfferResources("uuid-test", 1);
        ArgumentCaptor<DeleteObjectsRequest> deleteObjArg = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(amazonS3, times(1)).deleteObjects(deleteObjArg.capture());
        assertEquals(1, deleteObjArg.getValue().getKeys().size());
    }

    @Test
    void deleteOfferResources_shouldDoNothing_whenNoObjectFoundOnS3() {
        var listingMock = mock(ObjectListing.class);
        when(listingMock.getObjectSummaries())
                .thenReturn(List.of());
        when(amazonS3.listObjects(any(ListObjectsRequest.class)))
                .thenReturn(listingMock);

        amazonClientService.deleteOfferResources("uuid-test", 1);
        verify(amazonS3, times(0)).deleteObjects(any());
    }

}