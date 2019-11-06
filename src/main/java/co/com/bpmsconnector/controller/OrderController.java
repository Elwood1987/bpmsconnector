package co.com.bpmsconnector.controller;

import co.com.bpmsconnector.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping("/order")
public class OrderController {


    @Autowired
    private OrderService orderService;




    @PostMapping("/instanceBPMS")
    public HttpStatus instanceBPMS(){
        return orderService.instanceBPMS();
    }


    @Bean
    public RestTemplate rest() {
        return new RestTemplate();
    }
}
