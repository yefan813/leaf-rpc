package com.leaf.server.service.impl;

import com.leaf.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        log.info("=======Server Side service invoke========");
        return "Server-side:" + name;
    }
}
