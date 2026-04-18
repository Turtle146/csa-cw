package com.hisham.sensorapi.application;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api/v1") // sets the base URL for all endpoints to /api/v1
public class SensorApplication extends Application {
    // jersey automatically finds all our resource classes so nothing is needed here
}