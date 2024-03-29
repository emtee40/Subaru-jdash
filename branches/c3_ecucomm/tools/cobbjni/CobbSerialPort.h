/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_sourceforge_JDash_ecu_comm_CobbSerialPort */

#ifndef _Included_net_sourceforge_JDash_ecu_comm_CobbSerialPort
#define _Included_net_sourceforge_JDash_ecu_comm_CobbSerialPort
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     net_sourceforge_JDash_ecu_comm_CobbSerialPort
 * Method:    nativeStart
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeStart
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_sourceforge_JDash_ecu_comm_CobbSerialPort
 * Method:    nativeStop
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeStop
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_sourceforge_JDash_ecu_comm_CobbSerialPort
 * Method:    nativeRead
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeRead
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     net_sourceforge_JDash_ecu_comm_CobbSerialPort
 * Method:    nativeWrite
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativeWrite
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     net_sourceforge_JDash_ecu_comm_CobbSerialPort
 * Method:    nativePurge
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sourceforge_JDash_ecu_comm_CobbSerialPort_nativePurge
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
