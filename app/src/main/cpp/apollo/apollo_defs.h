/*
 *	NAME
 *		apollo_defs.h
 *
 *	DESCRIPTION
 *		This file defines the Apollo interface enumerated types and constants.
 *
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#ifndef APOLLO_DEFS_H
#define APOLLO_DEFS_H


typedef enum
{
	GVApolloMovie,
	GVApolloMusic,
	GVApolloNews,
	GVApolloSliver,
	GVApolloDefault,
	GVApolloNUMPrest	
}GVApolloPresets;

/* Apollo Input Modes */
typedef enum
{
	kSrsApolloInput1_0_0=0,			/* C */
	kSrsApolloInput2_0_0,			/* L/R */
	kSrsApolloInputLtRt,			/* Lt/Rt */
	kSrsApolloNumInputModes
} GVApolloInputMode;

/* Apollo CSDecOpt Processing Modes */
typedef enum
{
	kSrsApollokCSDecoderCinema=0,	/* C */
	kSrsApollokCSDecoderMusic,		/* L/R */
	kSrsApolloNumCSDecoderModes
} GVApolloCSDecoderMode;

#endif

