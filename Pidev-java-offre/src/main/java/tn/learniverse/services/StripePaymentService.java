package tn.learniverse.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentService {
    
    private static final String STRIPE_SECRET_KEY = "sk_test_51QzSoVJv3NyOjLLc6pEKPq6VLUQkCvlf0B5lG2hQKeJBHcuzhoctrm89FvIk4qPIvWcn3uTkT5ZFtTTEgbtFCmnB00WlvabUOb";
    
    public StripePaymentService() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }
    
    public String createPaymentIntent(double amount, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100)) // Convert to cents
                .setCurrency(currency.toLowerCase())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getClientSecret();
    }
} 