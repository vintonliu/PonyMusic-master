/*
 *	NAME
 *		sample.h
 *
 *	DESCRIPTION
 *		Define the type and word length for audio samples.
 *
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#ifndef SAMPLE_H
#define SAMPLE_H


/******  Boolean definitions  ******/

/* Get a consistent definition for TRUE and FALSE */
#undef FALSE
#undef TRUE
#define	FALSE					0
#define	TRUE					(!FALSE)

/* This type only needs to store one of two possible values:  TRUE or FALSE */
typedef int Bool;


/* These can be defined as nothing or as a compiler keyword, as appropriate */
#define	GVInline
#define	GVRegister				register


/******  Conditional compile definitions for predefined configurations  ******/


/* Primary configuation settings */

/* Resolution of internal audio processing */
#ifdef qFixed
#if qFixed
#define qFloat					FALSE
#else
#define qFloat					TRUE
#endif
#endif
#undef qFixed
#if defined(kSampleBits) && !defined(qFloat)
#define qFloat					FALSE
#endif
#if !defined(kSampleBits) && !defined(_GV_q16BIT) && !defined(_GV_q24BIT) && !defined(_GV_q32BIT) && !defined(_GV_qFLOAT)
#if defined(qFloat)
#if qFloat
#define _GV_qFLOAT
#else
#define _GV_q16BIT
#endif
#undef qFloat
#endif
#endif
#if defined(_GV_q16BIT)
#define kSampleBits				16
#define	qFloat					FALSE
#elif	defined(_GV_q24BIT)
#define	kSampleBits				24
#define	qFloat					FALSE
#elif	defined(_GV_q32BIT)
#define	kSampleBits				32
#define	qFloat					FALSE
#elif	defined(_GV_qFLOAT)
#define	kSampleBits				24
#define	qFloat					TRUE
#endif
#define	qFixed					(!(qFloat))

/* Resolution of audio I/O */
#define kInputSampleBits		16
#define kOutputSampleBits		16

/* Define each of the following two symbols to be TRUE or FALSE */
#define qInputFloat				FALSE	/* TRUE iff input is floating point */
#define qOutputFloat			FALSE	/* TRUE iff output is floating point */

#define qInputFixed				(!qInputFloat)
#define qOutputFixed			(!qOutputFloat)


/* Secondary configuation settings */

/*
 *	Review the following type definitions (typedefs) to ensure that
 *	the C types (short, long, etc.) for the compiler being used are
 *	consistent with the bit resolution shown.  For fixed point, the
 *	SampleAccumulator should have at least (2 * kSampleBits) bits.
 */

#if (qFixed)

/* Fixed point */
#if ((kSampleBits) <= 16)
typedef short Sample;
typedef long SampleAccumulator;
#else
typedef long Sample;
typedef __int64 SampleAccumulator;
#endif

#else

/* Floating point */
#if   ((kSampleBits) <= 24)
typedef float Sample;
#elif ((kSampleBits) <= 53)
typedef double Sample;
#else
typedef long double Sample;
#endif
typedef Sample SampleAccumulator;

#endif


#if (qInputFixed)

/* Fixed point */
#if   ((kInputSampleBits) <= 16)
typedef short SampleInput;
#elif ((kInputSampleBits) <= 32)
#if ((kInputSampleBits)==24)
typedef unsigned char SampleInput;
#else
typedef long SampleInput;
#endif
#else
typedef __int64 SampleInput;
#endif

#else

/* Floating point */
#if   ((kInputSampleBits) <= 24)
typedef float SampleInput;
#elif ((kInputSampleBits) <= 53)
typedef double SampleInput;
#else
typedef long double SampleInput;
#endif

#endif


#if (qOutputFixed)

/* Fixed point */
#if   ((kOutputSampleBits) <= 16)
typedef short SampleOutput;
#elif ((kOutputSampleBits) <= 32)
#if ((kOutputSampleBits)==24)
typedef unsigned char SampleOutput;
#else
typedef long SampleOutput;
#endif
#else
typedef __int64 SampleOutput;
#endif

#else

/* Floating point */
#if   ((kOutputSampleBits) <= 24)
typedef float SampleOutput;
#elif ((kOutputSampleBits) <= 53)
typedef double SampleOutput;
#else
typedef long double SampleOutput;
#endif

#endif


/****** Sample Conversion and Signal Processing macros ******/

/* Fixed point scale value */
#define kMaxVal2ToThe(n)				(n)

/* Clip() can be used even if kSoftwareClip is FALSE */
#define Clip(acc,min,max)				((acc) > max ? max : ((acc) < min ? min : (acc)))

/* Define sample value range limits for fixed and floating point */
#define kFixedMinimum(sampleBits)		(~0 << ((sampleBits) - 1))
#define kFixedMaximum(sampleBits)		(~kFixedMinimum(sampleBits))
#define kFloatMaximum					1.0
#define kFloatMinimum					(-(kFloatMaximum))
#if (qFixed)
#define kMinValue						kFixedMinimum(kSampleBits)
#define kMaxValue						kFixedMaximum(kSampleBits)
#define LeftShift(acc,shift)			((acc) << (shift))
#define SignedHalf(sign)				((sign) >= 0 ? 0.5 : -0.5)
#define ClipCoefficient(k)				Clip((k), kMinValue, kMaxValue)
#else
#define kMinValue						kFloatMinimum
#define kMaxValue						kFloatMaximum
#define LeftShift(acc,shift)			(acc)
#define SignedHalf(sign)				0
#define ClipCoefficient(k)				(k)
#endif

/* Macros to create sample values from floating point constants */
#if !defined(kSrsGainScale)
#define kSrsGainScale					kMaxVal2ToThe(0)
#endif
#if !defined(kSrsControlScale)
#define kSrsControlScale				kMaxVal2ToThe(0)
#endif
#define ConstantToWideSample(k,scale)	(														\
											(SampleAccumulator)									\
											(													\
												(k)												\
												*												\
												LeftShift										\
												(												\
													(SampleAccumulator)1,						\
													(kSampleBits) - 1 - (scale)					\
												)												\
												+												\
												SignedHalf(k)									\
											)													\
										)
#define ConstantToSample(k,scale)		((Sample)ClipCoefficient(ConstantToWideSample(k, scale)))
#define ConstantToGain(k)				ConstantToWideSample((k), kSrsGainScale)
#define ConstantToControl(k)			ConstantToWideSample((k), kSrsControlScale)


/******  Data structure to hold the audio samples for one sample period  ******/

/* Number of channels for various formats */
typedef enum 
{
	kSrsMono = 1,
	kSrs2_0,
	kSrsLRC,
	kSrsLRCS,
	kSrs5_0,
	kSrs5_1,
	kSrs6_1,
	kSrs7_1
} GVNumChannels;


/* Channel identification (used to index into channel array) */
typedef enum
{
	kSrsMonoChannel,
	kSrsLeft = 0,
	kSrsRight,
	kSrsLeftSurround,
	kSrsRightSurround,
	kSrsCenter,
	kSrsSub,
	kSrsCenterSurround,
    kSrsLeftBack = 6,
	kSrsRightBack,
	kSrsEnvelop,
	kSrsQuadCenter = 2,
	kSrsQuadSurround
} GVChannelId;

/* Channel structures */
/* These are defined as structs so that assignment will work */
typedef struct { Sample* mChannel[kSrsMono  ]; } GVMonoChannel;
typedef struct { Sample* mChannel[kSrs2_0   ]; } GVStereoChannel;
typedef struct { Sample* mChannel[kSrsLRC   ]; } GVLrcChannel;
typedef struct { Sample* mChannel[kSrsLRCS  ]; } GVLrcsChannel;
typedef struct { Sample* mChannel[kSrs5_0   ]; } GVFivePointZeroChannel;
typedef struct { Sample* mChannel[kSrs5_1   ]; } GVFivePointOneChannel;
typedef struct { Sample* mChannel[kSrs6_1   ]; } GVSixPointOneChannel;
typedef struct { Sample* mChannel[kSrs7_1   ]; } GVSevenPointOneChannel;


typedef union
{
	void*						pMultiChannel;
	GVSevenPointOneChannel*	pSevenPointOneChannel;
	GVSixPointOneChannel*		pSixPointOneChannel;
	GVFivePointOneChannel*		pFivePointOneChannel;
	GVFivePointZeroChannel*	pFivePointZeroChannel;
	GVLrcsChannel*				pLrcsChannel;
	GVLrcChannel*				pLrcChannel;
	GVStereoChannel*			pStereoChannel;
	GVMonoChannel*				pMonoChannel;
} GVMultiChannel;


/******  C++ Compatibility for Function Declarations  ******/

/* Use this around all function declarations */
#if defined(__cplusplus)
#define SrsBeginFunctionDeclarations	extern "C" {
#define SrsEndFunctionDeclarations		}
#else
#define SrsBeginFunctionDeclarations
#define SrsEndFunctionDeclarations
#endif


#endif
