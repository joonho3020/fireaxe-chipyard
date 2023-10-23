#include "mmio.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <riscv-pk/encoding.h>
#include "marchid.h"
#include "nic-rss.h"

#define NPACKETS 50
#define TEST_OFFSET 3
#define TEST_LEN 356
#define ARRAY_LEN 360
#define NTRIALS 3

/* #define NO_NIC_DEBUG */


static size_t N_CORES = 2;

static void __attribute__((noinline)) barrier()
{
  static volatile int sense;
  static volatile int count;
  static __thread int threadsense;

  __sync_synchronize();

  threadsense = !threadsense;
  if (__sync_fetch_and_add(&count, 1) == N_CORES-1)
  {
    count = 0;
    sense = threadsense;
  }
  else while(sense != threadsense)
    ;

  __sync_synchronize();
}

void run_nic(size_t coreid) {
  uint32_t* src[NPACKETS];
  uint32_t* dst[NPACKETS];
  for (int i = 0; i < NPACKETS; i++) {
    src[i] = (uint32_t*)malloc(sizeof(uint32_t) * ARRAY_LEN);
    dst[i] = (uint32_t*)malloc(sizeof(uint32_t) * ARRAY_LEN);
    for (int j = 0; j < ARRAY_LEN; j++) {
      src[i][j] = i * ARRAY_LEN + j;
    }
    uint64_t pkt_size = TEST_LEN * sizeof(uint32_t);
    uint64_t src_addr = (uint64_t)&src[i][TEST_OFFSET];
    uint64_t send_packet = (pkt_size << 48) | src_addr;
    uint64_t recv_addr = (uint64_t)dst[i];
#ifndef NO_NIC_DEBUG
    nic_send_req(coreid, send_packet);
    nic_set_recv_addr(coreid, recv_addr);
#endif
  }

  printf("Core %d finished memory setup\n", coreid);

#ifndef NO_NIC_DEBUG
  int ncomps, send_comps_left = NPACKETS, recv_comps_left = NPACKETS;
  while (send_comps_left > 0 || recv_comps_left > 0) {
    ncomps = nic_send_comp_avail(coreid);
    asm volatile ("fence");
    for (int i = 0; i < ncomps; i++)
      nic_send_comp(coreid);
    send_comps_left -= ncomps;
    printf("Core %d send ncomps: %d\n", coreid, ncomps);

    ncomps = nic_recv_comp_avail(coreid);
    asm volatile ("fence");
    for (int i = 0; i < ncomps; i++)
      nic_recv_comp(coreid);
    recv_comps_left -= ncomps;
    printf("Core %d recv ncomps: %d\n", coreid, ncomps);
  }
#endif
  for (int i = 0; i < NPACKETS; i++) {
    free(src[i]);
    free(dst[i]);
  }
  printf("Core %d finished NIC run\n", coreid);
}

void __main(void) {
  size_t mhartid = read_csr(mhartid);
  if (mhartid >= N_CORES) while (1);
  for (size_t i = 0; i < N_CORES; i++) {
    if (mhartid == i) {
      run_nic(mhartid);
    }
/* barrier(); */
  }
/* barrier(); */

  // Spin if not core 0
  if (mhartid > 0) while (1);
}

int main(void) {
  __main();
  return 0;
}
