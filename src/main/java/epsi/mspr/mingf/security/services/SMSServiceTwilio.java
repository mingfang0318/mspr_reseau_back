package epsi.mspr.mingf.security.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@Service
public class SMSServiceTwilio implements SMSService{
    // Find your Account Sid and Token at twilio.com/console
    @Value("${com.twilio.type.account.sid}")
    private final static String ACCOUNT_SID = "AC01ab3cfa898735aca2a3889076d07c38";
    @Value("${com.twilio.type.auth.token}")
    private final static String AUTH_TOKEN = "eeaf52d265a0335c0a2789acde09cf00";
    @Value("${com.twilio.type.PhoneNumber}")
    private final static String PHONE_NUMBER = "+18068355258";
    public String userPhone;
    public int code;

    public SMSServiceTwilio (){}

    public SMSServiceTwilio (String userPhone, int code){
        this.userPhone = userPhone;
        this.code = code;
    }

    @Override
    public Message sendSMS() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(userPhone),//The phone number you are sending text to
                        new com.twilio.type.PhoneNumber(PHONE_NUMBER),//The Twilio phone number
                        "Votre code de confirmation : " + code)
                .create();

        return message;
    }

}
