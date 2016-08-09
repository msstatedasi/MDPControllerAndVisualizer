/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.EdgeRenderer;
import prefuse.visual.VisualItem;

/**
 *
 * @author jlewis
 */
public class FinalEdgeRenderer extends AbstractShapeRenderer 
{
    protected EdgeRenderer m_edgeRenderer = new EdgeRenderer(); 

    @Override
    protected Shape getRawShape(VisualItem vi) 
    {
        return vi.getBounds();
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void render(Graphics2D g, VisualItem item)
    {
        Rectangle2D bounds = item.getBounds();
        double width = bounds.getBounds().width * 2;
        double height = bounds.getBounds().height;
//        bounds.set
        
        item.setSize(50);
        
        m_edgeRenderer.render(g, item);
    }
    
}
