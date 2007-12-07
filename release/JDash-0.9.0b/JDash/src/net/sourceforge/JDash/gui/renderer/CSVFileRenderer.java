/*******************************************************
 * 
 *  @author spowell
 *  CVSFileRenderer
 *  Aug 8, 2006
 *  $Id: CSVFileRenderer.java,v 1.2 2006/09/14 02:03:43 shaneapowell Exp $
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * User: tjennings
 * Date: Jun 2, 2005
 * Time: 7:33:38 PM
 */
public class CSVFileRenderer extends Renderer {    
    private File _f;
    private FileWriter _fw;

    public CSVFileRenderer(String f, List<Parameter> p) {
        super(p);
        /* File _f = */ new File(f);
    }

    public CSVFileRenderer(String f) {
        super();
        _f = new File(f);
    }

    public void setParams(List<Parameter> params) throws RenderException {
        super.setParams(params);
        try {
            initFile();
        } catch (IOException ioe) {
            throw new RenderException(ioe);
        }
    }

    public void shutDown() throws RenderException {
        try {
            if(_fw != null) _fw.flush(); _fw.close();
        } catch(IOException e) {
            throw new RenderException(e);
        }
    }

    private void initFile() throws IOException {
        if (_f.exists()) _f.delete();
        _fw = new FileWriter(_f);

        printHeader();
    }

    private void printHeader() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for(Parameter p : _params) {
            if (!first) sb.append(','); else first = false;
            sb.append('"').append(p.getName()).append('"');
        }
        sb.append('\n');
        _fw.write(sb.toString());
    }

    protected void render() throws RenderException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for(Parameter p : _params) {
            if (!first) sb.append(','); else first = false;
            sb.append('"').append(p.getResult()).append('"');
        }
        sb.append("\n");

        try {
            _fw.write(sb.toString());
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }




}
