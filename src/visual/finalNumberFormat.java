/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 *
 * @author jlewis
 */
public class finalNumberFormat extends NumberFormat {
    
    boolean x;
    
    finalNumberFormat(boolean x)
    {
        this.x = x;
    }
    
    
    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        StringBuffer buf = new StringBuffer();
        if(x) buf.append((int) number);
        else 
        {
            String s = Double.toString((double) Math.round(number*100000) / 100000);
            buf.append(s);
        }
       
        toAppendTo.append(buf);
        return buf;
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        StringBuffer buf = new StringBuffer();
        buf.append(number);
        return buf;
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
