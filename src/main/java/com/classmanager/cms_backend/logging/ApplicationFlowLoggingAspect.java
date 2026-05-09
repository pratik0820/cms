package com.classmanager.cms_backend.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
public class ApplicationFlowLoggingAspect {

    private static final Logger log = LogManager.getLogger(ApplicationFlowLoggingAspect.class);

    @Around("execution(public * com.classmanager.cms_backend.controller..*(..)) || execution(public * com.classmanager.cms_backend.service..*(..))")
    public Object logMethodFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String arguments = formatArguments(signature.getParameterNames(), joinPoint.getArgs());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Entering {}.{} args={}", className, methodName, arguments);

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.debug("Exiting {}.{} result={} durationMs={}",
                    className,
                    methodName,
                    summarizeValue(result),
                    stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable throwable) {
            stopWatch.stop();
            log.error("Exception in {}.{} durationMs={} message={}",
                    className,
                    methodName,
                    stopWatch.getTotalTimeMillis(),
                    throwable.getMessage(),
                    throwable);
            throw throwable;
        }
    }

    private String formatArguments(String[] parameterNames, Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return IntStream.range(0, args.length)
                .mapToObj(index -> summarizeArgument(parameterNames, index, args[index]))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String summarizeArgument(String[] parameterNames, int index, Object arg) {
        String name = parameterNames != null && index >= 0 && index < parameterNames.length
                ? parameterNames[index]
                : "arg" + index;
        return name + "=" + summarizeValue(arg);
    }

    private String summarizeValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof CharSequence sequence) {
            return maskIfSensitive("value", truncate(sequence.toString(), 120));
        }

        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }

        if (value instanceof Collection<?> collection) {
            return "Collection(size=" + collection.size() + ")";
        }

        if (value instanceof Map<?, ?> map) {
            return summarizeMap(map);
        }

        if (value.getClass().isArray()) {
            return "Array(length=" + Array.getLength(value) + ")";
        }

        if (value.getClass().getPackageName().startsWith("com.classmanager.cms_backend.dto")) {
            return summarizePojo(value);
        }

        return value.getClass().getSimpleName();
    }

    private String summarizeMap(Map<?, ?> map) {
        Map<String, Object> safeMap = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (count++ >= 10) {
                safeMap.put("...", "truncated");
                break;
            }
            String key = String.valueOf(entry.getKey());
            safeMap.put(key, maskIfSensitive(key, summarizeValue(entry.getValue())));
        }
        return safeMap.toString();
    }

    private String summarizePojo(Object object) {
        Map<String, Object> fields = new LinkedHashMap<>();
        Field[] declaredFields = object.getClass().getDeclaredFields();
        int count = 0;

        for (Field field : declaredFields) {
            if (count++ >= 12) {
                fields.put("...", "truncated");
                break;
            }
            field.setAccessible(true);
            try {
                Object rawValue = field.get(object);
                String fieldName = field.getName();
                fields.put(fieldName, maskIfSensitive(fieldName, summarizeFieldValue(rawValue)));
            } catch (IllegalAccessException ignored) {
                fields.put(field.getName(), "<inaccessible>");
            }
        }

        return object.getClass().getSimpleName() + fields;
    }

    private Object summarizeFieldValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Collection<?> collection) {
            return "Collection(size=" + collection.size() + ")";
        }
        if (rawValue instanceof Map<?, ?> map) {
            return "Map(size=" + map.size() + ")";
        }
        if (rawValue.getClass().isArray()) {
            return "Array(length=" + Array.getLength(rawValue) + ")";
        }
        if (rawValue instanceof CharSequence sequence) {
            return truncate(sequence.toString(), 120);
        }
        if (isSimpleValue(rawValue)) {
            return rawValue;
        }
        return rawValue.getClass().getSimpleName();
    }

    private boolean isSimpleValue(Object value) {
        return value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof java.time.temporal.Temporal
                || value instanceof java.util.UUID;
    }

    private String maskIfSensitive(String key, Object value) {
        if (value == null) {
            return null;
        }

        String normalizedKey = key == null ? "" : key.toLowerCase();
        if (normalizedKey.contains("password")
                || normalizedKey.contains("token")
                || normalizedKey.contains("secret")
                || normalizedKey.contains("authorization")) {
            return "***";
        }
        return String.valueOf(value);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
