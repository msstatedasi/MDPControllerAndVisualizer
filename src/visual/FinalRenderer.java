/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

/**
 * This class describes the shape of nodes in our state space.
 * @author jlewis
 */
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

public class FinalRenderer extends AbstractShapeRenderer
{
    //protected RectangularShape m_box = new Rectangle2D.Double();
    protected Ellipse2D m_box = new Ellipse2D.Double();
    
    @Override
    protected Shape getRawShape(VisualItem item) 
    {    
        m_box.setFrame(item.getX(), item.getY(), 
                           60, 
                           30);
        
        

        return m_box;
    }
}