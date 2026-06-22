#include "SecurePassEngine.h"

#include <math.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

static const char LOWERCASE[] = "abcdefghijklmnopqrstuvwxyz";
static const char UPPERCASE[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
static const char DIGITS[] = "0123456789";
static const char SPECIAL[] = "!@#$%^&*()-_=+[]{}|;:,.<>?";

static uint32_t random_below(uint32_t upper) {
    if (upper == 0) {
        return 0;
    }

    uint32_t value = 0;
    arc4random_buf(&value, sizeof(value));
    return value % upper;
}

static void shuffle_chars(char *buffer, int32_t length) {
    for (int32_t index = length - 1; index > 0; --index) {
        int32_t swap_index = (int32_t) random_below((uint32_t) (index + 1));
        char temp = buffer[index];
        buffer[index] = buffer[swap_index];
        buffer[swap_index] = temp;
    }
}

static char pick_from_pool(const char *pool) {
    return pool[random_below((uint32_t) strlen(pool))];
}

int32_t securepass_generate_password(
        int32_t length,
        int32_t include_special,
        char *buffer,
        int32_t buffer_size) {
    if (buffer == NULL || buffer_size <= 0 || length <= 0) {
        return -1;
    }
    if (length + 1 > buffer_size) {
        return -2;
    }

    const char *pools[4];
    int pool_count = 3;
    pools[0] = LOWERCASE;
    pools[1] = UPPERCASE;
    pools[2] = DIGITS;
    if (include_special != 0) {
        pools[3] = SPECIAL;
        pool_count = 4;
    }

    char charset[128];
    size_t charset_len = 0;
    for (int i = 0; i < pool_count; ++i) {
        size_t pool_len = strlen(pools[i]);
        if (charset_len + pool_len >= sizeof(charset)) {
            return -1;
        }
        memcpy(charset + charset_len, pools[i], pool_len);
        charset_len += pool_len;
    }

    int32_t guaranteed_count = length < pool_count ? length : pool_count;
    for (int32_t index = 0; index < guaranteed_count; ++index) {
        buffer[index] = pick_from_pool(pools[index]);
    }
    for (int32_t index = guaranteed_count; index < length; ++index) {
        buffer[index] = charset[random_below((uint32_t) charset_len)];
    }

    shuffle_chars(buffer, length);
    buffer[length] = '\0';
    return 0;
}

int32_t securepass_charset_size(int32_t include_special) {
    int32_t size = (int32_t) (sizeof(LOWERCASE) + sizeof(UPPERCASE) + sizeof(DIGITS) - 3);
    if (include_special != 0) {
        size += (int32_t) (sizeof(SPECIAL) - 1);
    }
    return size;
}

double securepass_calculate_entropy(const char *password) {
    if (password == NULL || password[0] == '\0') {
        return 0.0;
    }

    int has_lower = 0;
    int has_upper = 0;
    int has_digit = 0;
    int has_special = 0;

    for (const unsigned char *cursor = (const unsigned char *) password; *cursor != '\0'; ++cursor) {
        if (*cursor >= 'a' && *cursor <= 'z') {
            has_lower = 1;
        } else if (*cursor >= 'A' && *cursor <= 'Z') {
            has_upper = 1;
        } else if (*cursor >= '0' && *cursor <= '9') {
            has_digit = 1;
        } else {
            has_special = 1;
        }
    }

    int pool_size = 0;
    if (has_lower) {
        pool_size += (int) (sizeof(LOWERCASE) - 1);
    }
    if (has_upper) {
        pool_size += (int) (sizeof(UPPERCASE) - 1);
    }
    if (has_digit) {
        pool_size += (int) (sizeof(DIGITS) - 1);
    }
    if (has_special) {
        pool_size += (int) (sizeof(SPECIAL) - 1);
    }

    if (pool_size == 0) {
        return 0.0;
    }

    return (double) strlen(password) * log2((double) pool_size);
}
