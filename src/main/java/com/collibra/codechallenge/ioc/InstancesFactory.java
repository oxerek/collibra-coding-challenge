package com.collibra.codechallenge.ioc;

import io.vavr.control.Try;
import org.apache.commons.beanutils.ConstructorUtils;
import org.reflections.Reflections;

import java.util.List;

public class InstancesFactory {

    private static Reflections reflections = new Reflections("com.collibra.codechallenge");

    public static <TYPE> TYPE instance(
            Class<TYPE> interfaceClass,
            String implementationName,
            List<Object> arguments,
            List<Class> argumentsClasses
    ) {
        Class<? extends TYPE> implementationClazz = reflections.getSubTypesOf(interfaceClass).stream()
                .filter(aClass -> aClass.getSimpleName().equals(implementationName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no implementation for: " + interfaceClass.getSimpleName()));

        return Try.of(() -> ConstructorUtils.invokeConstructor(
                implementationClazz,
                arguments.toArray(new Object[arguments.size()]),
                argumentsClasses.toArray(new Class<?>[argumentsClasses.size()]))
        ).getOrElseThrow(throwable -> new RuntimeException(throwable));
    }
}