package com.buaa01.illumineer_backend.service;

import com.buaa01.illumineer_backend.entity.Message;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Map;

public interface MessageService {
    ArrayList<Message> sendMsg(Integer receiver1, Integer receiver2, Integer type) throws JsonProcessingException;

    ArrayList<Message> getMsg(Integer rid);
}