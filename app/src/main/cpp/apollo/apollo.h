/*
 *	NAME
 *		apollo.h
 *
 *	DESCRIPTION
 *		This file defines the Apollo data structures and interfaces.
 *
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#ifndef APOLLO_H
#define APOLLO_H

#include "sample.h"
#include "gvcommon.h"
#include "config.h"
#include "apollo.h"
#include "apollo_defs.h"


/*************************************************************/
/************                                     ************/
/************    Apollo Control Default Values    ************/
/************                                     ************/
/*************************************************************/


#define kSrsApolloEnableDefault				TRUE
#define kSrsApolloHeadphoneDefault			FALSE
#define	kSrsApolloInputModeDefault			kSrsApolloInputLtRt

#define kSrsApolloCSDecoderOptOuputGainDefault	MakeCoefficient(1.0, kApolloCSDecoderOptOutputGainScale)

/* Data structure for APOLLO state variables */
struct tGVApolloState
{
	
	Sample							mHLDelay[2*(100)];
	Sample							mHLTemp[100*100];
	Sample							mVolumeControlDelay[100];

	Sample							mHighPassFilterState_E[kSrs2_0][100];

};
typedef struct tGVApolloState GVApolloState;

/*******************************************/
/************                   ************/
/************  Data Structures  ************/
/************                   ************/
/*******************************************/


/* Data structure for APOLLO controls */
struct tGVApolloChannel
{
	/* Boolean states */
	Bool							mEnable;		/* True = Process, False = Bypass */

	/* Linear controls */
	Sample							mInputGain;		/* Input gain  */
	Sample							mOutputGain;	/* Output gain  */
	Sample							mBypassGain;	/* Bypass gain  */
	Sample							mHeadroomGain;	/* Headroom gain */
	GVApolloInputMode				mInputMode;
//	GVApolloProcessSelect			mProcessSelect;
//	GVApolloHPFposition			mHPFposition;

	Sample							mCSDecOutputGainLR;
	Sample							mCSDecOutputGainLsRs;
	Sample							mCSDecOutputGainC;
	Sample							mCSDecOutputGainSub;

	/* State Structure Pointer */
	GVApolloState*			mState;		/* Every Channel structure has a state pointer */

};
typedef struct tGVApolloChannel GVApolloChannel;

/********************************************************/
/************                                ************/
/************  Internal processing function  ************/
/************                                ************/
/********************************************************/

GVErr
GVApolloProcess(
	GVApolloChannel* tsc, 
	GVSixPointOneChannel* in, 
	GVStereoChannel* out, 
	Sample* temp, 
	int n
);

/* Internal functions */

void SetGVApolloTopLevelControlDefaults(GVApolloChannel* tsc);


#endif
