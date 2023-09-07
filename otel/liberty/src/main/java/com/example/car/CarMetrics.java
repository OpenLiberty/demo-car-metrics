package com.example.car;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import java.util.concurrent.atomic.AtomicInteger;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
public class CarMetrics {

    AtomicInteger count = new AtomicInteger(0);

    Meter meter;
    DoubleHistogram carSpeedSummary;
    LongHistogram driveDuration;
    Long startTimemills;
    Attributes driver = Attributes
            .builder()
            .put("driver", "Don")
            .build();

    
    LongCounter startCounter;

    public CarMetrics() { 
        AutoConfiguredOpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk
                .builder()
                .build();
        
        OpenTelemetry openTelemetry = sdk.getOpenTelemetrySdk();
        
        meter = openTelemetry.meterBuilder("instrumentation-for-car")
                .setInstrumentationVersion("1.0.0")
                .build();

        startCounter = meter.counterBuilder("start.driving").build();
        
        meter
                .gaugeBuilder("car.locked")
                .setDescription("car locked")
                .buildWithCallback(measurement -> {
                    measurement.record(CarController.lockState.get(), Attributes.empty());
                });

        meter
                .gaugeBuilder("car.speed")
                .setDescription("car speed")
                .buildWithCallback(measurement -> {
                    measurement.record(CarController.speed, Attributes.of(stringKey("driver"), "Don"));
                });

        carSpeedSummary = meter
                .histogramBuilder("car.speedSummary")
                .setDescription("Summary of car speed")
                .setUnit("km/h")
                .build();
        
        driveDuration = meter
                .histogramBuilder("drive.timer")
                .setUnit("ms")
                .ofLongs()
                .build();
        
    }

    public void init() {
    }

    void startDrive() {
        startCounter.add(1);
        startTimemills = System.currentTimeMillis();
    }

    void endDrive() {
        driveDuration.record(System.currentTimeMillis() - startTimemills);
    }

    void speedUpdate(int speed) {
        if (carSpeedSummary != null) {
            carSpeedSummary.record(speed, driver);
        }
    }

}
