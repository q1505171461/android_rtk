#include "rtklib.h"

/* whether the SDK is initialized */
extern int KPL_isInitialize;

/* initialize the SDK */
void KPL_initialize(const char *mode, KPL_IO::sta_t *sta, double cut, double intv);

/* finalize the SDK */
void KPL_finilize();

/* restart */
void KPL_restart();

/* input parent of configures files */
void KPL_setParentDirectory(const char *);

/* input observations */
int KPL_inputObs(KPL_IO::gtime_t, KPL_IO::obs_t *obs);

/* input SSR data */
void KPL_inputSsr(KPL_IO::ssr_t *ssr,int solid);

/* input broadcast ephemeris data */
void KPL_inputEph(KPL_IO::nav_t *nav, int psat, int offset);

/* input Ssr bias */
void KPL_inputSsrBias(KPL_IO::ssr_t *ssr);

/* input set the process interval, minimum is 1.0 s */
void KPL_setIntv(double intv);

/* output retrieve the status, current support:
    1) NMEA_GGA
    2) TO BE UPDATED
*/
void KPL_retrieve(const char *, char *buff, int len);
