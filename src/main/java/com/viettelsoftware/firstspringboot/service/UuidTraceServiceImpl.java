package com.viettelsoftware.firstspringboot.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidTraceServiceImpl implements TraceService {
    @Override
    public String getCurrentTraceId() {
        return UUID.randomUUID().toString();
    }
}
