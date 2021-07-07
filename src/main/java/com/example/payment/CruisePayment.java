package com.example.payment;

import com.example.featureflag.FeatureFlag;
import com.example.featureflag.SessionContext;

import java.util.*;

public class CruisePayment {

    private final SessionContext sessionContext;
    private final BookingResult bookingResult;
    private CurrentCWCTravelException currentException;

    public CruisePayment(SessionContext sessionContext, BookingResult bookingResult) {
        this.sessionContext = sessionContext;
        this.bookingResult = bookingResult;
    }

    public void processCruisePayment(PaymentInfo paymentInfo, //
                                     List<BookingFinancialDistributionInfo> financialDistributions, //
                                     CurrentCWCTravelException root) {

        this.currentException = root;
        // Split deposit payments
        Optional<PaymentInfo> costcoPaymentInfo;
        Optional<PaymentInfo> cruisePaymentInfo = Optional.empty();

        if(bookingResult.isIncludesCruiseItem()) {
            Map<String, ArrayList<CreditCardInfo>> creditCardsInfos =
                    getSplitDepositPayments(paymentInfo, financialDistributions);
            cruisePaymentInfo = getCruiseCreditCards(paymentInfo, creditCardsInfos);
            costcoPaymentInfo = getCostcoCreditCards(paymentInfo, creditCardsInfos);
        } else {
            costcoPaymentInfo = Optional.of(paymentInfo);
        }

        FraudDetectionData fraudDetection = (FraudDetectionData) bookingResult
                .getAttribute("BOOKING_RESULT_ATTRIBUTE_FRAUD_DETECTION_DATA");
        AuthorizationRequestsBuilder authorizationRequestsBuilder = 
                new AuthorizationRequestsBuilder(sessionContext, bookingResult.getBookingId())//
                .fraudDetectionData(fraudDetection);
        
        List<AuthorizationRequest_CST> cruiseCreditCardAuthorizationRequests =
                authorizationRequestsBuilder.buildAuthorizationRequestsForFullPayment(cruisePaymentInfo.orElse(null));

        List<AuthorizationRequest_CST> costcoCreditCardAuthorizationRequests =
                authorizationRequestsBuilder.buildAuthorizationRequestsForFullPayment(costcoPaymentInfo.orElse(null));

        List<ScheduledAuthorizationRequest_CST> costcoScheduledPaymentAuthorizationRequests = //
                authorizationRequestsBuilder.buildScheduledAuthorizationRequests(costcoPaymentInfo.orElse(null));

        final CruisePaymentTO cruisePaymentTO = new CruisePaymentTO(paymentInfo,
                costcoPaymentInfo,
                cruisePaymentInfo,
                cruiseCreditCardAuthorizationRequests,
                costcoCreditCardAuthorizationRequests,
                costcoScheduledPaymentAuthorizationRequests);

        FeatureFlag.of("ENABLE_PAYMENT_TOKEN_USAGE", 707, "US_en")
                .ifEnabled(cruiseCreditCardAuthorizationRequests,
                        costcoCreditCardAuthorizationRequests,
                        this::performAVSCheckOnAllCreditCards)
                .orElse(cruisePaymentTO, this::performPayment);
    }

    private Optional<PaymentInfo> getCostcoCreditCards(PaymentInfo paymentInfo,
                                                       Map<String, ArrayList<CreditCardInfo>> creditCardsInfos) {
        return Optional.empty();
    }

    private Optional<PaymentInfo> getCruiseCreditCards(PaymentInfo paymentInfo,
                                                       Map<String, ArrayList<CreditCardInfo>> creditCardsInfos) {
        return Optional.empty();
    }

    private Map<String, ArrayList<CreditCardInfo>> getSplitDepositPayments(PaymentInfo paymentInfo,
                                                                           List<BookingFinancialDistributionInfo> financialDistributions) {
        return Collections.emptyMap();
    }


    private void performAVSCheckOnAllCreditCards(List<AuthorizationRequest_CST> cruiseCreditCardAuthorizationRequests,
                                                 List<AuthorizationRequest_CST> costcoCreditCardAuthorizationRequests) {
        //do something...
    }

    private void performPayment(CruisePaymentTO cruisePaymentTO) {
        double cruisePaymentAmount = applyCruisePayment(cruisePaymentTO.paymentInfo,
                cruisePaymentTO.cruisePaymentInfo,
                cruisePaymentTO.cruiseCreditCardAuthorizationRequests);

        verifyCruisePaymentIsMade();

        if(hasCostcoCreditCards(cruisePaymentTO.costcoPaymentInfo)) {
            authorizeAndPurchaseBookingByCreditCard(cruisePaymentTO.costcoCreditCardAuthorizationRequests,
                    cruisePaymentTO.costcoScheduledPaymentAuthorizationRequests,
                    cruisePaymentAmount);
        } else {
            updateBookingInvoiced();
        }
    }

    private void authorizeAndPurchaseBookingByCreditCard(List<AuthorizationRequest_CST> costcoCreditCardAuthorizationRequests,
                                                         List<ScheduledAuthorizationRequest_CST> costcoScheduledPaymentAuthorizationRequests,
                                                         double cruisePaymentAmount) {
    }

    private void updateBookingInvoiced() {
    }

    private boolean hasCostcoCreditCards(Optional<PaymentInfo> costcoPaymentInfo) {
        return false;
    }

    private void verifyCruisePaymentIsMade() {
    }

    private double applyCruisePayment(PaymentInfo paymentInfo, Optional<PaymentInfo> cruisePaymentInfo, 
                                      List<AuthorizationRequest_CST> cruiseCreditCardAuthorizationRequests) {
        return 0.0;
    }

    private class CruisePaymentTO {

        PaymentInfo paymentInfo;
        Optional<PaymentInfo> costcoPaymentInfo;
        Optional<PaymentInfo> cruisePaymentInfo;
        List<AuthorizationRequest_CST> cruiseCreditCardAuthorizationRequests;
        List<AuthorizationRequest_CST> costcoCreditCardAuthorizationRequests;
        List<ScheduledAuthorizationRequest_CST> costcoScheduledPaymentAuthorizationRequests;

        public CruisePaymentTO(PaymentInfo paymentInfo,
                               Optional<PaymentInfo> costcoPaymentInfo,
                               Optional<PaymentInfo> cruisePaymentInfo,
                               List<AuthorizationRequest_CST> cruiseCreditCardAuthorizationRequests,
                               List<AuthorizationRequest_CST> costcoCreditCardAuthorizationRequests,
                               List<ScheduledAuthorizationRequest_CST> costcoScheduledPaymentAuthorizationRequests) {

            this.paymentInfo = paymentInfo;
            this.costcoPaymentInfo = costcoPaymentInfo;
            this.cruisePaymentInfo = cruisePaymentInfo;
            this.cruiseCreditCardAuthorizationRequests = cruiseCreditCardAuthorizationRequests;
            this.costcoCreditCardAuthorizationRequests = costcoCreditCardAuthorizationRequests;
            this.costcoScheduledPaymentAuthorizationRequests = costcoScheduledPaymentAuthorizationRequests;
        }
    }
}
