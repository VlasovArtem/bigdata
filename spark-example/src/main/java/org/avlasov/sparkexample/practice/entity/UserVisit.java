package org.avlasov.sparkexample.practice.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserVisit implements Serializable {

    private final String sourceIP;
    private final String destURL;
    private final String visitDate;
    private final float adRevenue;
    private final String userAgent;
    private final String countryCode;
    private final String languageCode;
    private final String searchWord;
    private final int duration;

}
