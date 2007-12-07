/*******************************************************
 * 
 *  @author s_powell
 *  ErrorDialog.java
 *  Aug 2, 2007
 *  $ID:$
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
package net.sourceforge.JDash.waba.error;

import waba.fx.Font;
import waba.ui.Control;
import waba.ui.ControlEvent;
import waba.ui.Event;
import waba.ui.MessageBox;


/*******************************************************
 * This class provides methods to create and display 
 * error messages to the user.
 ******************************************************/
public class ErrorDialog
{

	private static final String ERROR_TITLE = "Error";
	
	private static final String OK_BUTTON = "Ok";
	private static final String STACKTRACE_BUTTON = "Stack Trace";
	
	private static final Font font_ = new Font("Tiny", Font.PLAIN, 8);
	
	private MessageBox mb_ = null;
	private String title_ = null;
	private String message_ = null;
	private Exception e_ = null;
	protected int justify_ = Control.CENTER;
	
	/*******************************************************
	 * This class is accessable through static methods only.
	 * The static methods create an instance of a ErrorDialog
	 * for display
	 ******************************************************/
	private ErrorDialog(String title, String message, Exception e)
	{
		this.title_ = title;
		this.message_ = message==null?"":message;
		this.e_ = e;
	}

	
	/********************************************************
	 * Shows an exception only
	 *******************************************************/
	private static void showException(Exception e)
	{
		/* Create the exception string */
		String message = e.getClass().getName() + "|" + e.getMessage();
		
		for (int index = 0; index < e.getStackTrace().length; index++)
		{
			message += "|@ " + e.getStackTrace()[index].getFileName() + ":" + e.getStackTrace()[index].getLineNumber();
			
		}
		
		ErrorDialog d = new ErrorDialog(ERROR_TITLE, message, null);
		d.justify_ = Control.LEFT;
		d.displayError();
	}
	
	
	/*******************************************************
	 * Display a simple error message
	 * @param message IN - the message to display
	 *******************************************************/
	public static void showError(String message)
	{
		new ErrorDialog(ERROR_TITLE, message, null).displayError();
	}
	
	
	/********************************************************
	 * Display a simple error message, and the exceptions stack trace.
	 * @param parent IN - the parent window
	 * @param message IN - the message to display
	 * @param e IN - the exception to display
	 *******************************************************/
	public static void showError(String message, Exception e)
	{
		new ErrorDialog(ERROR_TITLE, message, e).displayError();
	}
	
	
	
	
	/*******************************************************
	 * @param title
	 * @param message
	 * @param e
	 *******************************************************/
	private void displayError()
	{
		String[] buttons = null;
		
		if (this.e_ == null)
		{
			buttons = new String[] {OK_BUTTON};
		}
		else
		{
			buttons = new String[] {OK_BUTTON, STACKTRACE_BUTTON};
		}
		
		/* Create the message box */
		this.mb_ = new MessageBox(this.title_, this.message_, buttons)
		{
			
			public void onEvent(Event event)
			{
				/* We only want pressed events */
				if (event.type == ControlEvent.PRESSED)
				{
					/* Respond only to buttons */
					if (event.target.equals(btns))
					{
						/* OK button */
						switch(btns.getSelected())
						{
							/* Ok button */
							default:
								mb_.unpop();
							break;
							
							/* Stack Trace */
							case 1:
								ErrorDialog.showException(e_);
							break;
						}
					}
				}
			}
		};
		
	
		this.mb_.setTextAlignment(this.justify_);
		this.mb_.setFont(this.font_);
		this.mb_.popupBlockingModal();
	}
	
	
//	/********************************************************
//	 * 
//	 *******************************************************/
//	private void displayException()
//	{
//		/* Create the exception string */
//		String message = this.e_.getClass().getName() + "|" + this.e_.getMessage();
//		
//		for (int index = 0; index < this.e_.getStackTrace().length; index++)
//		{
//			message += "|@ " + e_.getStackTrace()[index].getFileName() + ":" + e_.getStackTrace()[index].getLineNumber();
//			
//		}
//		
//		ErrorDialog.showError(message);
//	}
	
}
