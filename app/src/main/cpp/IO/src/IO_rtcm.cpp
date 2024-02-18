#include "include/IO_rtcm.h"
#include "include/rtklib_fun.h"
#include "include/Interface.h"
#include <android/log.h>
#define LOG_TAG "YourAppTag"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
namespace KPL_IO
{
    static rtcm_t s_rtcm_obs;
    static rtcm_t s_rtcm_eph;
    static rtcm_t s_rtcm_ssr;
    static bool s_isInit = false;
    extern void SDK_init(const char *mode, const char *ant, double *refx, double *enu, double cut, double intv, const char * path)
    {
        if (!s_isInit)
        {
            KPL_IO::sta_t sta;
            strcpy(sta.antdes, ant);
            memcpy(sta.pos, refx, sizeof(double) * 3);
            memcpy(sta.del, enu, sizeof(double) * 3);
            init_rtcm(&s_rtcm_obs);
            init_rtcm(&s_rtcm_eph);
            init_rtcm(&s_rtcm_ssr);
            KPL_setParentDirectory(path);
            KPL_initialize(mode, &sta, cut, intv);
            s_isInit = true;
        }
    }
    extern void SDK_terminate()
    {
        if (s_isInit)
        {
            s_isInit = false;
            free_rtcm(&s_rtcm_obs);
            free_rtcm(&s_rtcm_eph);
            free_rtcm(&s_rtcm_ssr);
            KPL_finilize();
        }
    }
    extern void SDK_restart()
    {
        KPL_restart();
    }
    extern void SDK_setIntv(double intv)
    {
        KPL_setIntv(intv);
    }

    int IO_inputObsData(uint8_t data)
    {
        int ret = 0,b_process = 0;
        if (0 == (ret = input_rtcm3(&s_rtcm_obs, data)))
            return 0;
        switch (ret)
        {
        case 1:
            /* observations */
            //打印数据和解算时间
            KPL_setIntv(2);
            char buff[1024];
            time2str(s_rtcm_obs.time,buff,2);
            b_process = KPL_inputObs(s_rtcm_obs.time, &s_rtcm_obs.obs);
            LOGD("---- s_rtcm_obs.time:%s", time_str(s_rtcm_obs.time,2));
            if ( b_process == 1 ){
                LOGD("---- s_rtcm_obs.time:%s: b_process==1",buff );
            }
            break;
        case 2:
            /* navigation ephemeris */
            KPL_inputEph(&s_rtcm_obs.nav, s_rtcm_obs.ephsat, s_rtcm_obs.ephset ? MAXRTKSAT : 0);
            break;
        default:
            /* just return */
            break;
        }
        return b_process;
    }
    int IO_inputEphData(uint8_t data)
    {
        int ret = 0;
        if (0 == (ret = input_rtcm3(&s_rtcm_eph, data)))
            return 0;
        switch (ret)
        {
        case 2:
            /* navigation ephemeris */
            KPL_inputEph(&s_rtcm_eph.nav, s_rtcm_eph.ephsat, s_rtcm_eph.ephset ? MAXRTKSAT : 0);
            break;
        default:
            /* just return */
            break;
        }
        return ret;
    }
    int IO_inputSsrData(uint8_t data)
    {
        int ret = 0;
        if (0 == (ret = input_rtcm3(&s_rtcm_ssr, data)))
            return 0;
        switch (ret)
        {
        case 10:
            /* ssr data */
            KPL_inputSsr(s_rtcm_ssr.ssr, s_rtcm_ssr.solid_ssr);
            break;
        case 20:
            /* upd data */
            KPL_inputSsrBias(s_rtcm_ssr.ssr);
            break;
        default:
            /* just return */
            break;
        }
        return ret;
    }
    void SDK_retrieve(const char *type, char *buff, int len)
    {
        KPL_retrieve(type, buff, len);
    }

    void SDK_setpath(const char *path)
    {
        KPL_setParentDirectory(path);
    }
}