/*
 *	NAME
 *		apollo_api.h
 *
 *	DESCRIPTION
 *		This file defines the Apollo coefficients and interfaces.
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#ifndef APOLLO_API_H
#define APOLLO_API_H


#include "sample.h"
#include "gvcommon.h"
#include "config.h"
#include "apollo_defs.h"

/* Data structures used internally by APOLLO */
struct tGVApolloChannel;
typedef struct tGVApolloChannel GVApolloChannel;
struct tGVApolloState;
typedef struct tGVApolloState GVApolloState;

/* Global Decoding Block Size */
#define kMaxBlockLength			256


/* Number of temporary buffers required for internal processing */

#ifndef _NO_CSDECODEROPT
#define kSrsApolloTempBuffers						20 /* kSrsCSDecoderOptTempBuffers(17) + 3 */
#else
#define kSrsApolloTempBuffers						10 /* kSrsTruSurroundHDTempBuffers(7) + 3 */
#endif

/* Define the number of filter bands and re-define the maximum if necessary */
#define kSrsParametricEqMaxNumBands					8

#define	kSrsHighPassMaxOrder						6

#define kApolloCSDecoderOptOutputGainScale		kMaxVal2ToThe(3)

#
SrsBeginFunctionDeclarations			/* C++ Compatibility */
#

/* APOLLO Processing */

GVErr
GVApollo (
	GVApolloChannel* tsc, 
	GVSixPointOneChannel* In, 
	GVStereoChannel* Out, 
	Sample* temp, 
	int n
);

/* Initialize the state structure and connect the control to it */
void GVApolloStateInit(GVApolloChannel* TSChannel, GVApolloState* TSState);

/* Initialize the channel structure, resulting in all controls having their default settings */
void GVApolloChannelInit(GVApolloChannel* TSChannel);


/* Size of data structures, in bytes */
int GetGVApolloChannelSize(void);
int GetGVApolloStateSize(void);

unsigned char GVApolloVersion(GVVersionComponent which);

#
SrsEndFunctionDeclarations							/* C++ Compatibility */
#


#endif
