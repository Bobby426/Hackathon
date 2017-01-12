package com.example.bobby.hackathon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created on 11.01.2017.
 */

public class HC_SR04 extends Thread {
    DataOutputStream os = null;
    DataInputStream is = null;
    BufferedReader bfr = null;

    private long startzeit = 0, endzeit = 0, dauer;
    private float finaled, oldd;
    ArrayList<Integer> list;



    //Konstruktor
    public HC_SR04() {

        finaled = 200.0f;


        String suPath="";
        boolean check = true;
        Process process = null;
        if (new File("/system/bin/su").exists()){
            suPath = "/system/bin/su";
        }
        if (new File("/system/xbin/su").exists()){
            suPath = "/system/xbin/su";
        }


        try {
            process = Runtime.getRuntime().exec(suPath);
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
            check = false;
        }

        try {
            //erzeugt Prozess aus der Laufzeitumgebung
            //Process process = Runtime.getRuntime().exec("getprop");
            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            bfr = new BufferedReader(new InputStreamReader(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPins() throws Throwable {

            //Ordner der die GPIO Datei enthält zum export angeben
            os.writeBytes("echo 219 > /sys/class/gpio/export\n");
            os.writeBytes("echo 214 > /sys/class/gpio/export\n");
            // direction der GPIO Pins festlegen
            os.writeBytes("echo out > /sys/class/gpio/gpio219/direction\n");
            os.writeBytes("echo in > /sys/class/gpio/gpio214/direction\n");
    }

    private long getTriggerTime()throws Throwable{
        int timeout1 = 2100, timeout0 = 2100;
        // misst die Dauer, für die ein Signal vom HC_ST04 empfangen wird
        boolean abbruch = false;
        //wartet bis etwas empfangen wird und startet dann den Timer
        while(!abbruch){
            os.writeBytes("cat /sys/class/gpio/gpio214/value\n");
            String in = bfr.readLine();
            if(in.equals("1")) {
                startzeit = System.nanoTime();
                abbruch = true;
            } else {
                if(timeout1 > 0) {
                    timeout1--;
                } else {
                    System.out.println("Timeout while waiting for 1");
                    abbruch = true;
                }
            }
        }
        abbruch = false;

        //wartet bis kein Signal mehr gesendet wird und stoppt dann den Timer
        while(!abbruch) {
            os.writeBytes("cat /sys/class/gpio/gpio214/value\n");
            String in = bfr.readLine();
            if(in.equals("0")) {
                endzeit = System.nanoTime();
                abbruch = true;
            } else {
                if(timeout0 > 0) {
                    timeout0--;
                } else {
                    System.out.println("Timeout while waiting for 0");
                    abbruch = true;
                }
            }
        }
        //System.out.println("Die Dauer des Signals beträgt: " + (endzeit - startzeit));
        dauer = (endzeit - startzeit) / 1000;
        return dauer;
    }

    private float getdistance()throws Throwable{
        long  localtime = 0;
        // setzt Signal für den Trigger
        os.writeBytes("echo 1 > /sys/class/gpio/gpio219/value\n");
        // Trigger braucht 0,01 ms
        Thread.sleep(0, 10000);
        os.writeBytes("echo 0 > /sys/class/gpio/gpio219/value\n");

       localtime = getTriggerTime();

        return localtime * 340.29f / (20000); //Distanz ausgeben
    }
    public void run() {
        System.out.println("Das Modul HC_SR04 wird ausgeführt");

        list = new ArrayList<Integer>();

        try{
            createPins();

            int counter =0;
            int distance; // gemessene Distanz
            int referenz; // Referenz Messwert
            int maxheight = 500; // in cm
            boolean canMeasure = true;
            while (true){
                //Für jeden Durchlauf müssen folgende Variablen zurückgesetzt werden
                int sum = 0;
                distance = 0;
                //messen
                distance = (int)getdistance();

                //Referenzwerte messen
                 if(list.size()<100){
                     list.add(distance);
                     if(list.size()==99){
                         System.out.println("Referenzwerte gesammelt");
                     };
                 }
                else {

                     for(int i = 0;i<list.size();i++){
                         sum= sum + list.get(i);
                         System.out.println();

                     }
                     referenz = sum/list.size();
                     if(canMeasure==false && (distance > referenz-10 && distance < referenz+10)){
                         canMeasure = true;
                     }

                     //Abstand muss mindestens 60 cm betragen, ebenfalls werden Ausreiser berücksichtigt
                     if(canMeasure ==true && (distance < (referenz-60) ||distance > (referenz+100) )){
                         //Besucher
                         counter++; // Person wird gezählt
                         System.out.println("Aktueller Messwert: " + distance);
                         System.out.println("Und es wurde eine Person gezählt: " +counter);
                         canMeasure= false;
                         Thread.sleep(1900);
                     }
                     else{// kein Besucher --> Referenz
                         if(distance<maxheight) {
                             list.remove(0);
                             list.add(distance);
                         }
                     }

                 }
                   Thread.sleep(80);

            }//while

        }
        catch (Throwable t) {
            t.printStackTrace();
        }

    }//run()

}//class


