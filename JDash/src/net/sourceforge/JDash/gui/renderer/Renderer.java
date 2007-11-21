/*******************************************************
 * 
 *  @author spowell
 *  Renderer
 *  Aug 8, 2006
 *  $Id: Renderer.java,v 1.2 2006/09/14 02:03:43 shaneapowell Exp $
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
import java.util.Observer;
import java.util.Observable;

public abstract class Renderer implements Observer {
    protected List<Parameter> _params;
    private int _count = 0;

    public static Renderer consoleRenderer() {
        return new ConsoleRenderer();
    }

    public static Renderer csvFileRenderer(String f) {
        return new CSVFileRenderer(f);
    }

    public Renderer(List<Parameter> params) {
        this._params = params;
        for(Parameter p : params) {
//            p.addObserver(this);
        }
    }

    public Renderer() {

    }

    public void setParams(List<Parameter> params) throws RenderException {
        if(_params != null) detach();
        _params = params;
        for(Parameter p : _params) {
//            p.addObserver(this);
        }
    }

    private void detach() {
         for(Parameter p : _params) {
//            p.deleteObserver(this);
        }
    }

    protected abstract void render() throws RenderException;

    public abstract void shutDown() throws RenderException;

    public void update(Observable o, Object arg) {
        if(_count == _params.size()) {
            try {
                render();
            } catch(RenderException re) {
                re.printStackTrace();
            }
            _count = 0;
        } else {
            _count++;
        }

    }
}
