package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;

public interface PaperStatsService {

    CustomResponse updateStats(Long pid, Integer stats);
}
