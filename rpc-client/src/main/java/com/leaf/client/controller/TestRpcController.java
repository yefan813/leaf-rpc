package com.leaf.client.controller;

import com.leaf.client.proxy.ProxyFactory;
import com.leaf.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Slf4j
public class TestRpcController {

    @GetMapping("/testRpc")
    public void test() throws Exception {
        HelloService helloService = ProxyFactory.create(HelloService.class);
        String result = helloService.hello("yefan");
        log.info("========client receive result:[{}]=======", result);
    }
}
