package com.buaa01.illumineer_backend.service;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public interface StormService {
    CompletableFuture<String> getStorm() throws URISyntaxException, IOException, ParserConfigurationException, SAXException;
}
