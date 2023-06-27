
#ifndef JLAS_API_H
#define JLAS_API_H

#include "laszip_api.h"

//#define LASZIP_API

#ifdef __cplusplus
extern "C"
{
#endif

typedef struct jlas_point
{
  laszip_I32 X;
  laszip_I32 Y;
  laszip_I32 Z;
  laszip_U16 intensity;
  laszip_U8 return_number;
  laszip_U8 number_of_returns;
  laszip_U8 classification;
  laszip_U8 user_data;
  laszip_U16 point_source_ID;
  laszip_F64 gps_time;
  laszip_U16 rgb[4];

} jlas_point_struct;

LASZIP_API laszip_I32 jlas_copy_point( laszip_point_struct* point_in, jlas_point_struct* point_out );

LASZIP_API laszip_I32 laszip_load_dll_from_path( const char* full_path );

LASZIP_API laszip_I32 laszip_load_dll_functions( void* pointer_dl  );

#ifdef __cplusplus
}
#endif

#endif /* JLAS_API_H */

