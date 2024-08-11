package com.auxby.productmanager.utils.service;

import com.auxby.productmanager.api.v1.commun.dto.DeepLinkResponse;
import com.auxby.productmanager.exception.DeepLinkGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchIOService {
    private static final String BRANCH_IO_URL = "https://api2.branch.io/v1/url";
    private static final String MARKETING_TITLE_FORMAT = "Auxby - %s";
    private static final String DESCRIPTION_FORMAT = "Auxby - %s";
    private static final String ALIAS_FORMAT = "auxby-%s-%s";
    private static final int LIFE_TIME_SECONDS = 604800;

    @Value("${application.branch}")
    private String branchIOKey;

    private static final RestTemplate restTemplate = new RestTemplate();


    public String createDeepLink(Integer offerId, String title, String imageUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var randomNumber = new Random().nextInt(2000) + 1;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("branch_key", branchIOKey);
        requestBody.put("channel", "facebook");
        requestBody.put("feature", "onboarding");
        requestBody.put("campaign", "new_offer_promotion");
        requestBody.put("stage", "new_offer");
        requestBody.put("tags", new String[]{"one", "two", "three"});
        requestBody.put("type", 1);
        requestBody.put("alias", ALIAS_FORMAT.formatted(title, randomNumber));
        requestBody.put("data", buildData(offerId, title, imageUrl));
        requestBody.put("duration", LIFE_TIME_SECONDS * 4);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<DeepLinkResponse> response = restTemplate.exchange(BRANCH_IO_URL, HttpMethod.POST, httpEntity, DeepLinkResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().url();
        }

        throw new DeepLinkGenerationException();
    }

    private Map<String, Object> buildData(Integer offerId, String title, String imageUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("$canonical_identifier", "content/123");
        data.put("$og_title", title);
        data.put("$og_description", DESCRIPTION_FORMAT.formatted(title));
        data.put("$og_image_url", imageUrl);
        data.put("$desktop_url", "https://www.auxby.ro");
        data.put("$offerId", offerId);
        data.put("custom_boolean", true);
        data.put("custom_string", "everything");
        data.put("custom_array", new int[]{1, 2, 3, 4, 5, 6});

        Map<String, String> customObject = new HashMap<>();
        customObject.put("random", "dictionary");

        data.put("custom_object", customObject);
        data.put("$marketing_title", MARKETING_TITLE_FORMAT.formatted(title));

        return data;
    }
}
