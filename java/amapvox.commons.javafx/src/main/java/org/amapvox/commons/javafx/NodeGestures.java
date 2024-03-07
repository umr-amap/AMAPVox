/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if parent is zoomed.
 * @author http://stackoverflow.com/users/1844265/roland
 */
public class NodeGestures {

    private DragContext nodeDragContext = new DragContext();
    private boolean yMovable = true, xMovable = true;
    
    PannableCanvas canvas;

    public NodeGestures( PannableCanvas canvas) {
        this.canvas = canvas;
    }

    public void setyMovable(boolean yMovable) {
        this.yMovable = yMovable;
    }
    
    public void setxMovable(boolean xMovable) {
        this.xMovable = xMovable;
    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return onMousePressedEventHandler;
    }

    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return onMouseDraggedEventHandler;
    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( !event.isPrimaryButtonDown())
                return;

            nodeDragContext.mouseAnchorX = event.getSceneX();
            nodeDragContext.mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            nodeDragContext.translateAnchorX = node.getTranslateX();
            nodeDragContext.translateAnchorY = node.getTranslateY();

        }

    };

    private final EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        
        @Override
        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( !event.isPrimaryButtonDown())
                return;

            double scale = canvas.getScale();

            Node node = (Node) event.getSource();

            if(xMovable){
                node.setTranslateX(nodeDragContext.translateAnchorX + (( event.getSceneX() - nodeDragContext.mouseAnchorX) / scale));
            }
            
            if(yMovable){
                node.setTranslateY(nodeDragContext.translateAnchorY + (( event.getSceneY() - nodeDragContext.mouseAnchorY) / scale));
            }

            event.consume();

        }
    };
}