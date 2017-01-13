package com.example.bobby.hackathon;

/**
 * Created by Jochen on 12.01.2017.
 */

public abstract class GPIOFactorySeife {
    GPIO c2gpio = null;

    public GPIO getGPIO(int number, String direction){
        GPIO c2gpio = null;
        c2gpio = createGPIO(number,direction);

        return c2gpio;
    }

    //factory method
    protected abstract GPIO createGPIO(int number, String direction);

}
