
/** COBBdriver.dll interface

	Brandon Anderson - 8/3/07
	COBB Tuning, Inc

 */

#ifdef WIN32
#  ifdef LIBRARY_EXPORTS
#    define LIBRARY_API __declspec(dllexport)
#  else
#    define LIBRARY_API __declspec(dllimport)
# endif
#else
# define LIBRARY_API
#endif

#if defined __cplusplus
extern "C"
{
#endif

enum CobbCommsEnum {
	COBB_ERR_SUCCESS = 0,
	COBB_ERR_NOTCONNECTED,
	COBB_ERR_CABLE,
	COBB_ERR_COMMS,
	COBB_ERR_TIMEOUT,
};

typedef int (*t_CobbCommsStart)();
LIBRARY_API int CobbCommsStart(); // returns session id or <0 for error

typedef void (*t_CobbCommsStop)(int);
LIBRARY_API void CobbCommsStop(int session);

typedef int (*t_CobbCommsRead)(int, unsigned char *, unsigned short);
LIBRARY_API int CobbCommsRead(int session, unsigned char *buf, unsigned short len); // returns byte read or <0 for error

typedef int (*t_CobbCommsWrite)(int, unsigned char *, unsigned short);
LIBRARY_API int CobbCommsWrite(int session, unsigned char *buf, unsigned short len); // returns bytes written or <0 for error

typedef void (*t_CobbCommsPurge)(int);
LIBRARY_API void CobbCommsPurge(int session);

#if defined __cplusplus
} // extern "C"
#endif
