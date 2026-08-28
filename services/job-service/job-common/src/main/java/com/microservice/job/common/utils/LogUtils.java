package com.microservice.job.common.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class LogUtils {

    public static void startLog(String className, String methodName){
        log.info("******* {} ******* {} ******* START *******", className, methodName);
    }

    public static void endLog(String className, String methodName){
        log.info("******* {} ******* {} ******* END *******", className, methodName);
    }

    public static void errorMessage(String className, String name, String value){
        log.error("Error Message :: Class Name --> {} :: Name --> {} :: value: {}", className, name, value);
    }

    public static void logStackTrace(Exception e){
        log.error("Error Message :: Class Name --> {} :: exception: {}", e.getClass().getSimpleName(), e);
    }

    public static void logInfoMessage(String message){
        log.info("Info Message:: Name --> {}", message);
    }

    public static void logPayload(String url, String payload){
        log.info("Info Message:: \nURL --> [ {} ], \npayload --> [ {} ]", url, payload);
    }
}