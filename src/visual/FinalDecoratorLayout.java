/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

/**
 *
 * @author jlewis
 */
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;


/**
 * This class is responsible for the labels on the nodes and edges.
 * @author jlewis
 */
public class FinalDecoratorLayout extends Layout
{
    public FinalDecoratorLayout(String group) {
        super(group);
    }
    
    /**
     * This is called every time a node or edge needs rendering
     * @param frac 
     */
    public void run(double frac) {
        if(m_vis == null) return;
        Iterator iter = m_vis.items(m_group);
        while ( iter.hasNext() ) {
            DecoratorItem decorator = (DecoratorItem)iter.next();
            VisualItem decoratedItem = decorator.getDecoratedItem();
            Rectangle2D bounds = decoratedItem.getBounds();
            
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            
            setX(decorator, null, x);
            setY(decorator, null, y);
        }
    }
}