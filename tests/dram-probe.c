#include <stdio.h>
#include <inttypes.h>
#include "mmio.h"

typedef uint64_t addr_t;


#define PAGESIZE_BYTES 4096

int main(void) {
  addr_t range = 0x80000000LL << 4LL;
  addr_t start = 0x80000000LL;
  addr_t end   = start + range;

  for (addr_t addr = start; addr < end; addr += PAGESIZE_BYTES * 4096) {
    printf("ACCESSING addr 0x%" PRIx64 "\n", addr);

    uint64_t wdata = addr + 0x5LL;
    reg_write64(addr, wdata);
    uint64_t rdata = reg_read64(addr);

    if (rdata != wdata) {
      printf("DRAM UNACCESSIBLE addr 0x%" PRIx64 " got 0x%" PRIx64 " expected 0x%" PRIx64 "\n",
          addr, rdata, wdata);
    }
  }

  return 0;
}
