/*******************************************************
 * 
 *  @author Gregory Ng
 *  BaseStream.java
 *  February 28, 2008
 *
Copyright (C) 2008  Gregory Ng

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

// CobbSerialPort.cpp
// Author: Gregory Ng
// Date:   8/1/2008
// 
// Java Native Interface adaptation of the Cobb USB serial port drivers


// Currently, only Windows is supported for the Cobb USB driver. :-(
////////////////////////////////////////////////////////////////////


// Included from the <jdk_path>/includes.
// You'll also need to include <jdk_path>/includes/win32
#include <jni.h>

#include "CobbSerialPort.h"
#include "AccessECU/Drivers/cobb_comms_interface.h"
#include <stdio.h>

// LoadLibraryA()
#include <afx.h>



//extern void	nuke();
//extern void	OutputString(CString sMessage,unsigned short usMessageType = 0);

int g_SessionID = -1;

void * g_library = NULL;

t_CobbCommsStart CobbCommsDLL_Start = NULL;
t_CobbCommsStop  CobbCommsDLL_Stop  = NULL;
t_CobbCommsRead  CobbCommsDLL_Read  = NULL;
t_CobbCommsWrite CobbCommsDLL_Write = NULL;
t_CobbCommsPurge CobbCommsDLL_Purge = NULL;




JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeStart
  (JNIEnv *jenv, jclass jc)
{

	// load dll
	g_library = LoadLibraryA("COBBdriver.dll");

	if ( g_library == NULL ) {
		return -1; // ERR_FAILED
	}

	// link symbols
	CobbCommsDLL_Start = (t_CobbCommsStart)GetProcAddress(static_cast<HMODULE>(g_library), "CobbCommsStart");
	CobbCommsDLL_Stop  = (t_CobbCommsStop) GetProcAddress(static_cast<HMODULE>(g_library), "CobbCommsStop");
	CobbCommsDLL_Read  = (t_CobbCommsRead) GetProcAddress(static_cast<HMODULE>(g_library), "CobbCommsRead");
	CobbCommsDLL_Write = (t_CobbCommsWrite)GetProcAddress(static_cast<HMODULE>(g_library), "CobbCommsWrite");
	CobbCommsDLL_Purge = (t_CobbCommsPurge)GetProcAddress(static_cast<HMODULE>(g_library), "CobbCommsPurge");

	if ( CobbCommsDLL_Start == NULL
			|| CobbCommsDLL_Stop == NULL
			|| CobbCommsDLL_Read == NULL
			|| CobbCommsDLL_Write == NULL
			|| CobbCommsDLL_Purge == NULL ) {
		return -1; // ERR_FAILED
	}

	g_SessionID = CobbCommsDLL_Start();

	if ( g_SessionID < 0 ) {
		return -2; //F_COMMS_ERROR
	}

	return 0;

}

JNIEXPORT void JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeStop
  (JNIEnv *jenv, jclass jc, jint nSessionID)
{

	if ( g_SessionID >= 0 ) {
		CobbCommsDLL_Stop(g_SessionID);
	}
	g_SessionID = -1;

	CobbCommsDLL_Start = NULL;
	CobbCommsDLL_Stop = NULL;
	CobbCommsDLL_Read = NULL;
	CobbCommsDLL_Write = NULL;
	CobbCommsDLL_Purge = NULL;	
	
	if ( g_library != NULL ) {
		//FreeLibrary(static_cast<HMODULE>(g_library));
		g_library = NULL;
	}


}
JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeRead
  (JNIEnv *jenv, jclass jc, jint nSessionID, jbyteArray jbuff, jint off, jint nLength)
{
	int nRead = 0;
	unsigned int dwResult = 0;

	// This is too much data for the CobbCommsRead routine.
	if (nLength <= 0 || nLength > 0xffff) return -1;

	jbyte* sBuff = new jbyte[nLength];


//TRYTRY


	if ( g_SessionID < 0 ) 
		return -1; //F_COMMS_ERROR;

	dwResult = CobbCommsDLL_Read(g_SessionID, (unsigned char*)sBuff, (unsigned short) nLength);

//CATCHCATCH("commSerial::Read()");

/*	if(bExceptionFlag == EXEPT_CONTINUE)
	{}
	if(bExceptionFlag == EXEPT_ABORT)
		return -2;
*/
	jenv->SetByteArrayRegion(jbuff, 0, nLength, sBuff);
	delete [] sBuff;

	return (unsigned short)dwResult;
}


JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeWrite
  (JNIEnv *jenv, jclass jc, jint nSessionID, jbyteArray jbuff, jint off, jint nLength)

{

	DWORD dwLength = 0;
	jbyte* sBuff = new jbyte[nLength];

	jenv->GetByteArrayRegion(jbuff, 0, nLength, sBuff);
	
//TRYTRY

	if ( g_SessionID < 0 ) {
		return -1; //F_COMMS_ERROR;
	}

	dwLength = CobbCommsDLL_Write(g_SessionID, (unsigned char*)sBuff, (unsigned short)nLength);

//CATCHCATCH("commSerial::Write()");
	delete [] sBuff;

/*	if(bExceptionFlag == EXEPT_CONTINUE)
	{}
	if(bExceptionFlag == EXEPT_ABORT)
		nuke();
*/
	return (long)dwLength;

}


JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativePurge
  (JNIEnv *jenv, jclass jc, jint nSessionID)

{
	if ( g_SessionID >= 0 ) {
		CobbCommsDLL_Purge(g_SessionID);
		return 0;
	} return -1;
}
