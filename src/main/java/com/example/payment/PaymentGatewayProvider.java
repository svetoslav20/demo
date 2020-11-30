package com.example.payment;

public interface PaymentGatewayProvider {

	PaymentResponse verify(PaymentRequest paymentRequest);

}
