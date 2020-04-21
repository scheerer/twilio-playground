package com.blendedsoftware.twilio.playground;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.messaging.Message;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import java.net.URI;

import static java.lang.String.format;
import static spark.Spark.*;

@Slf4j
public class Application {

    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    public static void main(String[] args) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        port(9080);
        staticFileLocation("/public");

        post("/sms", (Request request, Response response) -> {
            logRequest(request);
            String from = request.params("From");
            String messageBody = request.params("Body");

            System.out.println(format("Incoming SMS from[%s] : %s", from, messageBody));

            MessagingResponse messagingResponse = new MessagingResponse.Builder()
                    .message(new Message.Builder().addText("TwilioQuest rules").build())
                    .build();

            response.type("text/xml");
            return messagingResponse.toXml();
        });

        post("/call", (Request request, Response response) -> {
            logRequest(request);
            String to = request.queryParams("to");

            Call call = Call.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    URI.create("http://demo.twilio.com/docs/voice.xml"))
                    .create();

            return "Call is inbound!";
        });

        post("/message", (Request request, Response response) -> {
            logRequest(request);
            String to = request.queryParams("to");

            // have to avoid classname collision with the other `Message` used above
            com.twilio.rest.api.v2010.account.Message call = com.twilio.rest.api.v2010.account.Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    "Good luck on your Twilio quest!")
                    .create();

            return format("Message incoming! %s\n", call.getSid());
        });

        post("/hello", (Request request, Response response) -> {
            logRequest(request);

            Say one = new Say.Builder("Hello there! You have successfully configured a web hook.").build();
            Say two = new Say.Builder("Good luck on your Twilio quest!").build();

            VoiceResponse voiceResponse = new VoiceResponse.Builder().say(one).say(two).build();

            response.type("text/xml");
            return voiceResponse.toXml();
        });
    }

    static void logRequest(Request request) {
        log.info("Incoming HTTP Request: ", request.toString());
    }
}
