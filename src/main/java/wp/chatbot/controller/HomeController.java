package wp.chatbot.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import wp.chatbot.BinancePrice;
import wp.chatbot.ChatMessage;

@Controller
@RequestMapping
public class HomeController {

    private Logger logger = LogManager.getLogger(HomeController.class);
    private SimpMessagingTemplate messagingTemplate;
    private static final String BINANCE_TICKER_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private RestTemplateBuilder restTemplateBuilder;

    public HomeController(SimpMessagingTemplate messagingTemplate,
                          RestTemplateBuilder restTemplateBuilder) {
        this.messagingTemplate = messagingTemplate;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @GetMapping()
    public String home() {
        return "home";
    }

    @MessageMapping("/chat.messages")
    public void send(@Payload ChatMessage chatMessage) {
        logger.info(chatMessage.toString());
        try {
            Double price = requestBinanceTicker(BINANCE_TICKER_URL + chatMessage.getContent());
            chatMessage.setContent(price.toString());
        } catch (Exception e) {
            chatMessage.setContent(e.getMessage());
        }
        messagingTemplate.convertAndSend("/topic/messages", chatMessage);
    }

    private Double requestBinanceTicker(String url) throws Exception {
        try {
            ResponseEntity<BinancePrice> responseEntity = restTemplateBuilder.build().exchange(url,
                    HttpMethod.GET,
                    null, BinancePrice.class);
            if (responseEntity == null || responseEntity.getBody() == null)
                throw new Exception("Invalid equation");

            return Double.parseDouble(responseEntity.getBody().getPrice());
        } catch (Exception e) {
            logger.error(e);
            throw new Exception("Cannot get price from url. Invalid symbol");
        }
    }

}
