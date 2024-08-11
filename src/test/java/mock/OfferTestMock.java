package mock;

import com.auxby.productmanager.api.v1.offer.model.*;
import com.auxby.productmanager.api.v1.user.model.UserDetails;
import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.CurrencyType;
import com.auxby.productmanager.utils.enums.OfferType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public interface OfferTestMock {

    List<Integer> OFFER_CATEGORIES = List.of(100, 101);
    String OFFER_TITLE = "Vintage Camera";
    String OFFER_LOCATION = "New York";
    String OFFER_DESCRIPTION = "A rare vintage camera in good condition.";
    Long OFFER_CATEGORY = 100L;
    UserDetails OFFER_OWNER = new UserDetails("Doe", "Joe", "joe.doe@gmail.com", null, new Date(), 5);
    ContactInfo OFFER_CONTACT_INFO = new ContactInfo("0740121221", "Cluj");
    List<CategoryDetails> OFFER_DETAILS = List.of(new CategoryDetails("Color", "Black"));

    static OfferInfo mockOfferInfo(OfferType offerType, String currency) {
        var auctionEndDate = OfferType.AUCTION.equals(offerType) ? new Timestamp(new Date(System.currentTimeMillis() + 86400000L).getTime()) : null;

        return new OfferInfo(
                "UnitTest On Sale",
                "This is a unit test offer",
                ConditionType.USED,
                OFFER_CATEGORY,
                offerType,
                BigDecimal.valueOf(500L),
                CurrencyType.getCurrencyType(currency),
                auctionEndDate,
                OFFER_CONTACT_INFO,
                OFFER_DETAILS,
                5,
                false,
                ""
        );
    }

    static OfferSummary mockOfferSummary() {
        return new OfferSummary(
                1L, // id
                OFFER_TITLE, //title
                OFFER_LOCATION, // location
                OFFER_DESCRIPTION,  // description
                OFFER_CATEGORY,                                 // categoryId
                new Date(),                           // publishDate
                new Date(System.currentTimeMillis() + 86400000L), // expirationDate
                new Date(),                           // auctionStartDate
                new Date(System.currentTimeMillis() + 432000000L), // auctionEndDate
                true,                                 // isOnAuction
                new BigDecimal("150.00"),             // highestBid
                Collections.emptyList(),              // bids
                new BigDecimal("200.00"),             // price
                "USD",
                "$",                         // currencyType
                OFFER_OWNER, // owner
                Collections.emptyList(),              // photos
                true,                                 // isUserFavorite
                "Used",                               // condition
                "Available",                          // status
                false,                                // isPromoted
                false,                                // autoExtend
                ""
        );
    }

    static DetailedOfferResponse mockDetailedOffer() {
        return new DetailedOfferResponse(
                1L,                                   // id
                "Classic Vintage Camera",             // title
                "Berlin, Germany",                    // location
                "A well-preserved vintage camera from the 1950s.", // description
                101L,                                 // categoryId
                new Date(),                           // publishDate
                new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L), // expirationDate, one week from now
                new Date(),                           // auctionStartDate
                new Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L), // auctionEndDate, two weeks from now
                true,                                 // isOnAuction
                new BigDecimal("1200.00"),            // price
                "EUR",
                "E",                         // currencyType
                OFFER_OWNER, // owner
                new BigDecimal("1100.00"),            // highestBid
                List.of(), // bids
                List.of(), // photos
                new ArrayList<>(), // details
                150,                                  // viewsNumber
                10,                                   // setAsFavoriteNumber
                true,                                 // isUserFavorite
                "Active",                             // status
                false,                                // isPromoted
                true,                                 // autoExtend
                "Excellent",                          // condition
                "+49 123 4567890",                    // phoneNumbers
                "https://unit-test.com/deeplink"
        );
    }
}
