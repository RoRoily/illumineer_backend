package com.buaa01.illumineer_backend.service.paper;

import java.util.concurrent.CompletableFuture;

public interface StormService {
    CompletableFuture<String> getStorm();
}
