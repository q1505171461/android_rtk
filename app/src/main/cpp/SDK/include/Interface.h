#include "rtklib.h"

/* whether the SDK is initialized */
extern int KPL_isInitialize;

/* config_path */
extern const char* KPL_config_path;

/* initialize the SDK */
extern void KPL_initialize(const char *mode, KPL_IO::sta_t *sta, double cut, double intv);

/* finalize the SDK */
extern void KPL_finilize();

/* restart */
extern void KPL_restart();

/* input parent of configures files */
void KPL_setParentDirectory(const char *);

/* input observations */
extern int KPL_inputObs(KPL_IO::gtime_t, KPL_IO::obs_t *obs);

/* input SSR data */
extern void KPL_inputSsr(KPL_IO::ssr_t *ssr, int solid);

/* input broadcast ephemeris data */
extern void KPL_inputEph(KPL_IO::nav_t *nav, int psat, int offset);

/* input Ssr bias */
extern void KPL_inputSsrBias(KPL_IO::ssr_t *ssr);

/* set cutoff for observations */
extern void KPL_setCutoff(double cutoff);

/* input set the process interval, minimum is 1.0 s */
extern void KPL_setIntv(double intv);

/* output retrieve the status, current support:
    1) NMEA_GGA
    2) TO BE UPDATED
*/
extern void KPL_retrieve(const char *, char *, int);