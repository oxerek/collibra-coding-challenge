package com.collibra.codechallenge;

import com.collibra.codechallenge.ioc.Configuration;

public class Application {

    public static void main(String[] args) {
        Configuration.server().start();
    }
}
