/*
 *	NAME
 *		apollo.c
 *
 *	DESCRIPTION
 *		This is a C implementation of GV Labs' Apollo algorithm.
 *
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#include "apollo.h"

/* Release Version Number */
static const unsigned char kSrsReleaseVersion[4] = { 1, 3, 2, 0 };


/****** Apollo() ******/

GVErr
GVApollo (
	GVApolloChannel* TSChannel, 
	GVSixPointOneChannel*	TSIn, 
	GVStereoChannel*		TSOut, 
	Sample*					temp, 
	int						n) 

{
	/* Process with Apollo */
	GVApolloProcess(TSChannel, TSIn, TSOut, temp, n);

	return kSrsOk;
}


/****** ApolloProcess() ******/

GVErr 
GVApolloProcess_StudioSound 
(
	GVApolloChannel* TSChannel, 
	GVSixPointOneChannel*	TSIn, 
	GVStereoChannel*		TSOut, 
	Sample*					temp, 
	int						n
)
{
	int	i;

	for (i = 0; i < n; i++)
	{
		TSOut->mChannel[kSrsLeft][i]			= TSIn->mChannel[kSrsLeft][i];
		TSOut->mChannel[kSrsRight][i]			= TSIn->mChannel[kSrsRight][i];
	}

	return kSrsOk;

};

/****** ApolloProcess() ******/

GVErr 
GVApolloProcess 
(
	GVApolloChannel* TSChannel, 
	GVSixPointOneChannel*	TSIn, 
	GVStereoChannel*		TSOut, 
	Sample*					temp, 
	int						n
)
{

	GVApolloProcess_StudioSound(TSChannel, TSIn, TSOut, temp, n);


	return kSrsOk;
};

/* Channel Init */
void GVApolloChannelInit(GVApolloChannel *tsc)//, GVApolloState *tss) {
{
	return;
}


/****** ApolloChannelSize() ******/
int 
GetGVApolloChannelSize(void)
{
	return sizeof(GVApolloChannel);
}

/****** ApolloStateSize() ******/
int 
GetGVApolloStateSize(void)
{
	return sizeof(GVApolloState);
}

/****** APOLLO Version() ******/

unsigned char
GVApolloVersion(GVVersionComponent which)
{
	return (which >= 0 && which < kSrsNumVersionComponents) ? kSrsReleaseVersion[which] : '\0';
}





