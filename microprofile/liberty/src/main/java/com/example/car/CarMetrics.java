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
package com.example.car;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetadataBuilder;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.Timer.Context;
import org.eclipse.microprofile.metrics.annotation.RegistryScope;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class CarMetrics {

    final MetricRegistry registry;
    final AtomicInteger last_roll = new AtomicInteger(0);
    final AtomicInteger last_felled = new AtomicInteger(0);
    
    AtomicInteger count = new AtomicInteger(0);
    
    Timer driveTimer = null;
    Context driveTimerContext = null; 
    
    Metadata carSpeedSummaryMetadata = null;
    
    public CarMetrics() { 
        registry=null; 
        // System.out.println("CombatMetrics2() ctor");    
    }
        
    @Inject
    public CarMetrics(@RegistryScope(scope = "carScope") MetricRegistry registry) {				// 3 - Inject a MetricRegistry with custom scope
        // System.out.println("CarMetrics ctor with Inject");
    	this.registry = registry;

        registry.gauge("car.locked", () -> CarController.lockState.get(), (Tag[])null);			// 4 - Register a gauge to track a value from a method
        registry.gauge("car.speed", () -> CarController.speed, new Tag("driver","Don"));					// 5 - Register a gauge to track a value from a primitive
        
        carSpeedSummaryMetadata = new MetadataBuilder()											// 6a - Use metadata to decorate a metric
				.withName("car.speedSummary")
				.withUnit("kph")
				.withDescription("Summary of car speed")
                .build();
    } 

    public void init() {}
        
    void startDrive() {
    	driveTimer = registry.timer("car.driveTime");											// 7a - Use a timer and hang onto context to stop later
    	driveTimerContext = driveTimer.time();
    }

    void endDrive() {
    	if (driveTimerContext != null)
    		driveTimerContext.stop();															// 7b - stop timer context to record duration since time()
    }
    
    void speedUpdate(int speed) {
    	registry.histogram(carSpeedSummaryMetadata, new Tag("driver","Don")).update(speed);		// 6b - Register a histogram to summarize a value
    }

}
