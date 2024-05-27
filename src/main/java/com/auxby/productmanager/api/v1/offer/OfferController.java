package com.auxby.productmanager.api.v1.offer;

import com.auxby.productmanager.api.v1.commun.SuccessResponse;
import com.auxby.productmanager.api.v1.offer.model.*;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearch;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearchCriteria;
import com.auxby.productmanager.utils.SecurityContextUtil;
import com.auxby.productmanager.utils.SimplePage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.auxby.productmanager.utils.constant.AppConstant.BASE_V1_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_V1_URL)
@Tag(name = "Offers APIs", description = "REST - endpoints for Offers.")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(summary = "Get all offers by filters.")
    public SimplePage<OfferSummary> getAllProducts(@ParameterObject @PageableDefault(sort = "id") Pageable pageable,
                                                   @ParameterObject OfferSearchCriteria searchCriteria) {
        log.info("GET - products summary by filters {}.", searchCriteria.toString());
        return offerService.getAllOffers(pageable, searchCriteria);
    }

    @GetMapping("/getByIds")
    public List<OfferSummary> getAllProductsByIds(@RequestParam List<Integer> id) {
        log.info("GET - products summary by list of ids.");
        return offerService.getOffersByIds(id);
    }

    @GetMapping("/{id}")
    public DetailedOfferResponse getProductId(@PathVariable Integer id,
                                              @RequestParam(required = false) boolean increaseView) {
        log.info("GET - product by id:{}", id);
        return offerService.getOfferById(id, increaseView);
    }

    @GetMapping("/user/my-offers")
    public List<OfferSummary> getAllUserOffers() {
        log.info("GET - all user offers");
        return offerService.getAllUserOffers(SecurityContextUtil.getUsername());
    }

    @GetMapping("/category")
    public List<CategoryInfo> getOfferCategory() {
        log.info("GET - offer category.");
        return offerService.getOfferCategory();
    }

    @GetMapping("/favorites")
    public List<OfferSummary> getFavoriteOffers() {
        log.info("GET - get favorite offers");
        return offerService.getFavoriteOffers();
    }

    @GetMapping("/promoted")
    public List<OfferSummary> getPromotedOffers() {
        log.info("GET - get promoted offers");
        return offerService.getPromotedOffers();
    }

    @GetMapping("/search-summary")
    public SearchSummaryInfo searchOfferInCategories(@RequestParam String offerTitle) {
        log.info("GET - Search for offer {} in all categories.", offerTitle);
        return offerService.searchOfferInCategories(offerTitle);
    }

    @PostMapping
    @Operation(summary = "Add a new offer.")
    public OfferSummary createProduct(@Valid @RequestBody OfferInfo offerInfo) {
        log.info("POST - create product.");
        return offerService.createOffer(offerInfo);
    }

    @PostMapping("/upload/{offerId}")
    public boolean uploadOfferImages(@RequestParam("files") MultipartFile[] files,
                                     @PathVariable Integer offerId) {
        log.info("POST - Upload offer images.");
        return offerService.uploadOfferImages(offerId, files, SecurityContextUtil.getUsername());
    }

    @PostMapping("/search")
    public List<OfferSummary> advancedSearch(@RequestBody OfferSearch searchCriteria) {
        log.info("POST - advanced search by criteria:{}", searchCriteria);
        return offerService.advancedSearch(searchCriteria);
    }

    @PostMapping("/{offerId}/favorite")
    public SuccessResponse setFavoriteOffer(@PathVariable Integer offerId) {
        log.info("POST - add/remove offer from favorite.");
        offerService.addRemoveFavoriteOffer(offerId);
        return new SuccessResponse();
    }

    @PostMapping("/{offerId}/promote")
    public SuccessResponse promoteOffer(@PathVariable Integer offerId,
                                        @Valid @RequestBody PromoteInfo promoteInfo) {
        log.info("POST - Promote offer.");
        offerService.promoteOffer(offerId, promoteInfo);
        return new SuccessResponse();
    }

    @PutMapping("/{id}")
    public OfferSummary updateProduct(@Valid @RequestBody UpdateOfferInfo offerInfo,
                                      @PathVariable Integer id) {
        log.info("PUT - update product.");
        return offerService.updateProduct(offerInfo, id, SecurityContextUtil.getUsername());
    }

    @PutMapping("/{offerId}/changeStatus")
    public SuccessResponse changeOfferStatus(@PathVariable Integer offerId,
                                             @RequestBody @Valid ChangeStatusRequest changeStatusRequest) {
        log.info("PUT - change offer current status.");
        offerService.changeOfferState(offerId, changeStatusRequest.requiredCoins(), SecurityContextUtil.getUsername());
        return new SuccessResponse();
    }

    @DeleteMapping("/{id}")
    public boolean deleteProductById(@PathVariable Integer id) {
        log.info("DELETE - product by id:{}", id);
        return offerService.deleteOffer(id, SecurityContextUtil.getUsername());
    }
}
