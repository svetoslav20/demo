package com.example.payment;

import com.example.featureflag.SessionContext;

import java.util.Collections;
import java.util.List;

public class AuthorizationRequestsBuilder {
    public AuthorizationRequestsBuilder(SessionContext sessionContext, Object bookingId) {

    }

    public AuthorizationRequestsBuilder fraudDetectionData(FraudDetectionData fraudDetection) {
        return null;
    }

    public List<AuthorizationRequest_CST> buildAuthorizationRequestsForFullPayment(PaymentInfo orElse) {
        return Collections.emptyList();
    }

    public List<ScheduledAuthorizationRequest_CST> buildScheduledAuthorizationRequests(PaymentInfo orElse) {
        return Collections.emptyList();
    }
}
