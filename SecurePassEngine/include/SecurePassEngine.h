#ifndef SECUREPASS_ENGINE_H
#define SECUREPASS_ENGINE_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

int32_t securepass_generate_password(
    int32_t length,
    int32_t include_special,
    char *buffer,
    int32_t buffer_size);

double securepass_calculate_entropy(const char *password);

int32_t securepass_charset_size(int32_t include_special);

#ifdef __cplusplus
}
#endif

#endif
