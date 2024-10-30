package com.buaa01.illumineer_backend.service;

import com.buaa01.illumineer_backend.entity.Message;

import java.util.ArrayList;
import java.util.Map;

public interface MessageService {
    ArrayList<Message> sendMsg(Integer receiver1, Integer receiver2, Integer type);

    ArrayList<Message> getMsg(Integer rid);
}