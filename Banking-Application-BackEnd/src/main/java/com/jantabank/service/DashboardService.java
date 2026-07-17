package com.jantabank.service;

import com.jantabank.dto.dashboard.DashboardResponse;

public interface DashboardService {

    /** Aggregates the home-dashboard view for the given authenticated username. */
    DashboardResponse getDashboard(String username);
}
