/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 */

package net.sourceforge.JDash.logger;
import java.io.*;
import java.util.Date;

/**
 * A class that logs events.
 * @author greg
 */
public class StreamTraceLog {
    //protected BufferedOutputStream osTraceLog = null;
    protected OutputStreamWriter   osTraceLogWriter = null;
    protected File fileTrace = null;
    
    long dateStart=0; // base time
    
    byte nLogLevel = 1;
    
    //////////////////////////////////
    // Trace capability.  Haven't used this yet.
    // Still thinking about how this would work.
    // Write a trace of input/output
    public boolean isOpen()
    {
        return !(osTraceLogWriter == null);
    }
    
    public void open(String filename) throws IOException
    {
        close();  // close, if open.
        fileTrace = new File(filename);
        osTraceLogWriter = 
            new OutputStreamWriter(
            new BufferedOutputStream(
            new FileOutputStream(fileTrace)
            )
            );
        Date d = new Date();
        dateStart = d.getTime();
        
        osTraceLogWriter.write("Trace log opened at " + d.toString() + "\n");
        osTraceLogWriter.flush();
    }
    
    
    public void close() throws IOException
    {
        if (osTraceLogWriter != null) 
        {
            osTraceLogWriter.close();
            osTraceLogWriter = null;
        }
    }    
    
    
    
    public synchronized void logDataEvent(String src, byte[] data, int offset, int len)
    {
        if (osTraceLogWriter == null) return;
        String s;
        long d = (new Date()).getTime();
        s = String.format("[%9.3f] %-8s", (float)(d - dateStart) / 1000.0, src);
        
        try {
        
            osTraceLogWriter.write(s);
            int i;
            switch (nLogLevel) 
            {
            case 1:
                osTraceLogWriter.write(String.format(" %d bytes", len));
                break;
            case 2:
                        
                for (i=0; i < len; i++)
                {
                    if (i > 0 && (i & 0xff) == 0) 
                        osTraceLogWriter.write("\n        ");
                    osTraceLogWriter.write(String.format(" %02x", data[i]));
                }
                break;
            }
            osTraceLogWriter.write("\n");
            osTraceLogWriter.flush();
        } 
        catch (IOException e)
        {
            // Just suppress the exception.
        }
    }

	/*******************************************************
	 * Override
	 * @see java.lang.Object#finalize()
	 *******************************************************/
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}    
}
