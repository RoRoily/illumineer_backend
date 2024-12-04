package com.buaa01.illumineer_backend.service;

import java.util.concurrent.CompletableFuture;

public interface StormService {
    CompletableFuture<String> getStorm();
}
