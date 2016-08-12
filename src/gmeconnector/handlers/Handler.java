/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public interface Handler {
    
    public Object handle(JBuilderObject o);
    
}
