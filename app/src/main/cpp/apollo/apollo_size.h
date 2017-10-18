/*
 *	NAME
 *		apollo_size.h
 *
 *	DESCRIPTION
 *      This header file defines the structure sizes for the
 *      APOLLO control and state buffers.
 *      The control structure size is named kSrsApolloChannelSize, and
 *      the state buffer size is names kSrsApolloStateSize.
 *
 *      The actual value changes depending on the various preprocessor
 *      definitions that specify the sample size.
 */

#if !defined(APOLLO_SIZE_H)
#define APOLLO_SIZE_H

/* APOLLO Structure Sizes */

#if _GV_q16BIT	/* Not Supported */
/* Structure sizes for 16-bit samples */
#endif
#if _GV_q24BIT
/* Structure sizes for 24-bit samples */
#define kSrsApolloChannelSize    888
#define kSrsApolloStateSize      46152
#endif
#if _GV_q32BIT
/* Structure sizes for 32-bit samples */
#define kSrsApolloChannelSize    888
#define kSrsApolloStateSize      46088
#endif
#if _GV_qFLOAT
/* Structure sizes for floating point samples */
#define kSrsApolloChannelSize    888
#define kSrsApolloStateSize      55936
#endif


/*
** If the values were not defined, then there is a
** configuration error.
*/
#if !kSrsApolloChannelSize
#error Invalid channel configuration!
#endif

#if !kSrsApolloStateSize
#error Invalid state configuration!
#endif

#endif  /* APOLLOSIZE_H */