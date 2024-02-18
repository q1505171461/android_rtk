#ifndef IO_RTKLIB_IO_RTCM_H_
#define IO_RTKLIB_IO_RTCM_H_
#include "include/rtklib.h"
namespace KPL_IO
{
    extern void SDK_init(const char *mode, const char *ant, double *refx, double *enu, double cut, double intv, const char * path);
    extern void SDK_terminate();
    extern void SDK_restart();
    extern void SDK_setIntv(double intv);
    extern void SDK_setpath(const char *path);
    extern void SDK_retrieve(const char *, char *, int len);
    extern int IO_inputObsData(uint8_t data);
    extern int IO_inputEphData(uint8_t data);
    extern int IO_inputSsrData(uint8_t data);
}
#endif