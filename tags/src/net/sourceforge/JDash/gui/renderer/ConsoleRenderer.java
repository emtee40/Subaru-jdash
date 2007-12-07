/*******************************************************
 * 
 *  @author spowell
 *  ConsoleRenderer
 *  Aug 8, 2006
 *  $Id: ConsoleRenderer.java,v 1.2 2006/09/14 02:03:43 shaneapowell Exp $
 *
Copyright (C) 2006  Shane Powell

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ******************************************************/
package net.sourceforge.JDash.gui.renderer;

import net.sourceforge.JDash.ecu.param.Parameter;

import java.util.List;

public class ConsoleRenderer extends Renderer {
    private static final String SPACE = " ";

    public ConsoleRenderer(List <Parameter> p) {
        super(p);
    }

    public ConsoleRenderer() {
        super();
    }

    public void shutDown() throws RenderException {
        //do nothing
    }

    protected void render() {
        StringBuilder sb = new StringBuilder();

        for(Parameter p : _params) {
            sb.append(p.getName()).append(":").append(SPACE);
            sb.append(p.getResult()).append(SPACE);
        }
        sb.append("\n");
        System.out.println(sb.toString());
    }
}
