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

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/car")
@ApplicationScoped
public class CarController {

    static Random random = new Random(2342);

    static final int LOCKSTATE_UNLOCKED = 0;
    static final int LOCKSTATE_LOCKED = 1;
    static AtomicInteger lockState = new AtomicInteger(LOCKSTATE_LOCKED);

    static final int DRIVESTATE_PARK = 0;
    static final int DRIVESTATE_DRIVE = 1;
    static AtomicInteger driveState = new AtomicInteger(DRIVESTATE_PARK);

    static AtomicBoolean speedThreadStarted = new AtomicBoolean(false);
    static float speed = 0.0f;
    static float targetSpeed = 0.0f;

    final CarMetrics metrics;

    public CarController() {
        this.metrics = null;
        // System.out.println("CarController ctor");
    }

    @Inject
    public CarController(CarMetrics metrics) { // 1 - encapsulate things related to car metrics in CarMetrics
        this.metrics = metrics;
        // System.out.println("CarController(CarMetrics) ctor");
        startSpeedThread();
        metrics.init();
    }

    @Path("lock")
    @GET
    public String lock() {
        lockState.set(LOCKSTATE_LOCKED);
        return "lock";
    }

    @Path("unlock")
    @GET
    public String unlock() {
        lockState.set(LOCKSTATE_UNLOCKED);
        return "unlock";
    }

    @Path("startDrive")
    @GET
    public String startDrive() {
        if (driveState.get() == DRIVESTATE_PARK) {
            driveState.set(DRIVESTATE_DRIVE);
            targetSpeed = 115.0f;
            metrics.startDrive();
            return "driving...";
        }
        return "already driving";
    }

    @Path("endDrive")
    @GET
    public String endDrive() {
        if (driveState.get() == DRIVESTATE_DRIVE) {
            driveState.set(DRIVESTATE_PARK);
            targetSpeed = 0.0f;
            metrics.endDrive();
            return "parking...";
        } else {
            return "already parked";
        }
    }

    @Path("start")
    @GET
    public String start() {
        startSpeedThread();
        return "start";
    }

    @Path("stop")
    @GET
    public String stop() {
        endDrive();
        stopSpeedThread();
        return "stop";
    }

    private void startSpeedThread() {
        // avoid starting a new thread if a thread is already running
        if (speedThreadStarted.getAndSet(true))
            return;

        speed = 0.0f;

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (speedThreadStarted.get()) {
                    System.out.println("speed:" + speed);
                    int targetSpeedVar = (int) (0.3 * targetSpeed);
                    float newSpeed = targetSpeed
                            + ((targetSpeedVar > 0) ? (float) random.nextInt(targetSpeedVar) : 0.0f);
                    speed = speed * .8f + newSpeed * .2f;
                    if (targetSpeed == 0 && speed < 5.0f) {
                        speed = 0.0f;
                    }

                    try {
                        Thread.sleep(1000);
//                      if (speed > 0.0f) {
                        metrics.speedUpdate((int) speed);
//                      }
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void stopSpeedThread() {
        speedThreadStarted.set(false);
    }

}