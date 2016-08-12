/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.controllers;

/**
 *
 * @author stefano
 */
public class FinalStateException extends Exception {
    
    public FinalStateException() {
        super("The system is already in final state");
    }
    
}
