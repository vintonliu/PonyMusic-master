/*
 *	NAME
 *		apollo_default.h
 *
 *	DESCRIPTION
 *		This file contains the definitions for setting the "Default"
 *		filter configuration of GV Apollo TruEQ and HPF.
 *
 *		THIS FILE HAS BEEN AUTOMATICALLY GENERATED.
 *
 *		This software may only be used and/or reproduced under license
 *		from GV Labs, Inc.  Any full or partial reproduction of this
 *		software source code shall retain this notice.
 */


#ifndef APOLLO_DEFAULT_H
#define APOLLO_DEFAULT_H


#ifndef APOLLO_DEFAULT_API_H
#include "apollo_default_api.h"
#endif
#include "apollo_api.h"


#
SrsBeginFunctionDeclarations			/* C++ Compatibility */
#


/******  Accessor functions for advanced initialization; see apollo_api.h for more information  ******/

Sample* GetGVApolloParametricEqDefaultCoefPtr32k(void);	/* For use with GVApolloParametricEqParametricEqStateFineControlInitCustom() */
Sample* GetGVApolloParametricEqDefaultCoefPtr44k(void);	/* For use with GVApolloParametricEqParametricEqStateFineControlInitCustom() */
Sample* GetGVApolloParametricEqDefaultCoefPtr48k(void);	/* For use with GVApolloParametricEqParametricEqStateFineControlInitCustom() */
Sample* GetGVApolloHighPassFilterDefaultCoefPtr32k(void);						/* For use with GVApolloParametricEqHighPassFilterStateInitCustom() */
Sample* GetGVApolloHighPassFilterEndDefaultCoefPtr32k(void);					/* For use with GVApolloParametricEqHighPassFilterEndStateInitCustom() */
Sample* GetGVApolloHighPassFilterDefaultCoefPtr44k(void);						/* For use with GVApolloParametricEqHighPassFilterStateInitCustom() */
Sample* GetGVApolloHighPassFilterEndDefaultCoefPtr44k(void);					/* For use with GVApolloParametricEqHighPassFilterEndStateInitCustom() */
Sample* GetGVApolloHighPassFilterDefaultCoefPtr48k(void);						/* For use with GVApolloParametricEqHighPassFilterStateInitCustom() */
Sample* GetGVApolloHighPassFilterEndDefaultCoefPtr48k(void);					/* For use with GVApolloParametricEqHighPassFilterEndStateInitCustom() */


#
SrsEndFunctionDeclarations				/* C++ Compatibility */
#


#endif
