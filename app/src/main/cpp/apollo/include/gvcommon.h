/*
 *	NAME
 *		gvcommon.h
 *
 *	DESCRIPTION
 *		Define types and macros used by all modules.
 *
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#ifndef GVCOMMON_H
#define GVCOMMON_H


#include "sample.h"


/* Define NULL if it has not already been defined */
#ifndef NULL
#ifdef __cplusplus
#define NULL    0
#else
#define NULL    ((void *)0)
#endif
#endif


/* GV Error Codes */
typedef enum
{
	kSrsOk,
	kSrsControlOutOfRange,
	kSrsNotSupported,
	kSrsError,
	kSrsIncompatibleParameter,
	kSrsVersionMismatch,
	kSrsSizeMismatch,
	kSrsNumErrs
} GVErr;


/* Assert macros are disabled unless qSrsErrorCheck is defined as TRUE */
/* Use GVAssertTrue if you want the result to be TRUE when error checking is turned off. */
/* Use GVAssertFalse if you want the result to be FALSE when error checking is turned off. */
#if qSrsErrorCheck
#define GVAssertTrue(a)	(a)
#define GVAssertFalse(a)	(a)
#if !defined(GVIsValidPtr)
/*
 *	If your system has a more robust pointer verification function, you can 
 *	define GVIsValidPtr(a) on the compiler command line to refer to that 
 *	function instead.  Usually, this is done with a "-D" argument; for 
 *	example, add "-DGVIsValidPtr(a)=(!(MyOSIsBadPtr(a)))" in your make or 
 *	project file if your system has a function called "MyOSIsBadPtr" that 
 *	returns TRUE for bad pointers.
 */
#define GVIsValidPtr(a)	((a)!=0)
#endif
#else
#define GVAssertTrue(a)	1
#define GVAssertFalse(a)	0
#define GVIsValidPtr(a)	1
#endif


/* GV Version Number Components */
typedef enum
{
	kSrsVersionMajor,
    kSrsVersionMinor,
    kSrsVersionRevision,
    kSrsVersionRelease,
	kSrsNumVersionComponents
} GVVersionComponent;


/* GV Sample Rate List */
/*
 *	This structure is used to pass information among GV modules about which 
 *	sample rates are supported by the technologies.  This is used, for 
 *	example, to tell the command line parser what sample rates to accept, 
 *	or to tell the filter design code the set of sample rates for which to 
 *	export coefficients.
 *
 *	A common way to set this up is to define an enumerated list of supported 
 *	sample rates in the xxx_defs.h file for the technology:
 *
 *		typedef enum
 *		{
 *			kXxxSampleRate32k = 32000,
 *			kXxxSampleRate44k = 44100,
 *			kXxxSampleRate48k = 48000,
 *			kXxxNumSampleRates = 3		// Update this if the number of rates is changed
 *		} GVXxxSupportedSampleRates;
 *
 *	and then define a structure in the main C source file that is compatible 
 *	with the GVSampleRateList structure:
 *
 *		typedef struct
 *		{
 *			int				mNumSampleRates;
 *			unsigned long*	mSampleRate;
 *			unsigned long	mRateList[kXxxNumSampleRates];
 *		} GVXxxSampleRateList;
 *
 *		static GVXxxSampleRateList sRateList =
 *		{
 *			kXxxNumSampleRates,
 *			sRateList.mRateList,
 *			{
 *				kXxxSampleRate32k,
 *				kXxxSampleRate44k,
 *				kXxxSampleRate48k
 *			}
 *		};
 *
 *	This could be done in a simpler way by using "flexible array members" 
 *	where the last element is an array whose size is unspecified or set 
 *	to zero or one, but that requires a language extension that varies 
 *	in syntax from compiler to compiler, so it would be non-portable.
 *
 *	The GVSampleRateList structure type could also be used to provide 
 *	a query function to return information about which sample rates are 
 *	supported by the technology:
 *
 *		const GVSampleRateList* GetGVXxxSampleRates(void)
 *		{
 *			return &sRateList;
 *		}
 */
typedef struct
{
	int				mNumSampleRates;
	unsigned long*	mSampleRate;
} GVSampleRateList;


#
SrsBeginFunctionDeclarations			/* C++ Compatibility */
#


void
GVMemSet
(
	void*	mem,		/* Pointer to memory to be set */
	int		size,		/* Size in bytes (chars) */
	char	value		/* Value to set (pass in zero to clear memory) */
);


void
GVMemCopy
(
	void*	dest,		/* Pointer to memory destination */
	void*	src,		/* Pointer to memory source */
	int		size		/* Number of bytes (chars) to copy */
);


/*
 *	Technology Version Number
 *
 *	To determine the version of the technology release, call this
 *	function once for each component of the full version number.
 *	To display the full version number as a string, use decmimal
 *	notation and separate the components with a period.  For example,
 *	the first standard release of a technology typically has the
 *	version string "1.0.0.0".
 *
 *	kSrsVersionMajor
 *
 *	The major version number of a technology will change if
 *	backward compatibility with previous versions is broken.
 *
 *	kSrsVersionMinor
 *
 *	The minor version is incremented upon each new release of the
 *	software that includes new features, but maintains backward
 *	compatibility with the previous release.
 *
 *	kSrsVersionRevision
 *
 *	The revision is incremented upon each new release that includes
 *	minor modifications or fixes, but no additional new features.
 *	Revision increments also maintain backward compatibility
 *	with the previous release.
 *
 *	kSrsVersionRelease
 *
 *	The release number is either zero or 255 for standard releases along
 *	the main development path.  The value 255 should be considered to be
 *	equivalent to the value zero.  Other number ranges for the release have
 *	the following meanings:
 *
 *		  1 -  99	Reserved for internal use by GV Labs, Inc.
 *		100 - 199	Alpha release
 *		200 - 254	Beta release
 *
 *	Depending on the purpose of the release, nonzero release numbers may or
 *	may not maintain compatibility with the corresponding standard release.
 *
 */
unsigned char GVCommonLibVersion(GVVersionComponent which);


#
SrsEndFunctionDeclarations				/* C++ Compatibility */
#


#endif