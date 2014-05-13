package org.atmosphere.samples.chat;

import org.atmosphere.cpr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Chat implements AtmosphereHandler {
    private final Logger logger = LoggerFactory.getLogger(Chat.class);

    private JacksonDecoder decoder = new JacksonDecoder();
    private JacksonEncoder encoder = new JacksonEncoder();

    @Override
    public void onRequest(AtmosphereResource r) throws IOException {
        AtmosphereRequest req = r.getRequest();
        AtmosphereResponse res = r.getResponse();
        String method = req.getMethod();

        // Suspend the response.
        if ("GET".equalsIgnoreCase(method)) {
            res.setContentType("application/json;charset=UTF-8");
            r.suspend(-1);
        } else if ("POST".equalsIgnoreCase(method)) {
            String message = req.getReader().readLine();

            if (message != null) {
                Message msg = decoder.decode(message);
                logger.info("{} just send {}", msg.getAuthor(), msg.getMessage());
                r.getBroadcaster().broadcast(encoder.encode(msg));
            }
        }
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResource r = event.getResource();
        AtmosphereResponse res = r.getResponse();

        if (event.isSuspended()) {
            String body = event.getMessage().toString();

            res.getWriter().write(body);
            switch (r.transport()) {
                case JSONP:
                case AJAX:
                case LONG_POLLING:
                    event.getResource().resume();
                    break;
                default:
                    res.getWriter().flush();
                    break;
            }
        }
    }

    @Override
    public void destroy() {
    }
}