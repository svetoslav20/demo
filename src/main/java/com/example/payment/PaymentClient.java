package com.example.payment;

public class PaymentClient {

	public void makePaymentWithCreditCard() {
		PaymentGatewayProvider paymentProvider = new AccertifyCreditCardProvider();
		PaymentRequest paymentRequest = new CreditCardPayment();
		PaymentResponse paymentResponse = paymentProvider.verify(paymentRequest);
	}

	public void makePaymentWithToken() {
		PaymentGatewayProvider paymentProvider = new AccertifyTokenProvider();
		PaymentRequest paymentRequest = new TokenPayment();
		PaymentResponse paymentResponse = paymentProvider.verify(paymentRequest);
	}

}
