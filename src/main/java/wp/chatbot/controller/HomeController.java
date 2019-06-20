package wp.chatbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;
import wp.chatbot.BinancePrice;
import wp.chatbot.ChatMessage;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping
public class HomeController {

    private static final String GOOGLE_TRANSLATE = "http://translate.google.com.vn/translate_a/single";

    private Logger logger = LogManager.getLogger(HomeController.class);
    private SimpMessagingTemplate messagingTemplate;
    private ObjectMapper mapper = new ObjectMapper();
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
            String text = requestTranslate(chatMessage.getContent());
            chatMessage.setContent(text);
        } catch (Exception e) {
            chatMessage.setContent(e.getMessage());
        }
        messagingTemplate.convertAndSend("/topic/messages", chatMessage);
    }

    private String requestTranslate(String keyword) throws Exception {
        try {
            String url = GOOGLE_TRANSLATE + "?client=gtx&sl=auto&tl=vi&dt=t&dt=md&hl=auto&q=" + keyword.trim().replace(" ", "+");

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> responseEntity = restTemplateBuilder.build().exchange(url,
                    HttpMethod.GET,
                    entity,
                    List.class);

            List items = responseEntity.getBody();
            if (items != null && items.size() > 0) {
                List l1 = (List) items.get(0);
                List l2 = (List) l1.get(0);
                return l2.get(0).toString();
            }
            return "Nope. Try again!";
        } catch (Exception e) {
            logger.error(e);
            throw new Exception("Cannot get price from url. Invalid symbol");
        }
    }

}
