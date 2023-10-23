#include <stdint.h>

#define SIMPLENIC_BASE 0x10016000L
#define SIMPLENIC_SEND_REQ_OFFSET  0
#define SIMPLENIC_RECV_REQ_OFFSET  8
#define SIMPLENIC_SEND_COMP_OFFSET 16
#define SIMPLENIC_RECV_COMP_OFFSET 18
#define SIMPLENIC_COUNTS_OFFSET    20

#define SIMPLENIC_SEND_REQ(idx)  (SIMPLENIC_BASE + SIMPLENIC_SEND_REQ_OFFSET  + idx * 24)
#define SIMPLENIC_RECV_REQ(idx)  (SIMPLENIC_BASE + SIMPLENIC_RECV_REQ_OFFSET  + idx * 24)
#define SIMPLENIC_SEND_COMP(idx) (SIMPLENIC_BASE + SIMPLENIC_SEND_COMP_OFFSET + idx * 24)
#define SIMPLENIC_RECV_COMP(idx) (SIMPLENIC_BASE + SIMPLENIC_RECV_COMP_OFFSET + idx * 24)
#define SIMPLENIC_COUNTS(idx)    (SIMPLENIC_BASE + SIMPLENIC_COUNTS_OFFSET    + idx * 24)

static inline void nic_send_req(int id, uint64_t pkt) {
  reg_write64(SIMPLENIC_SEND_REQ(id), pkt);
}

static inline void nic_set_recv_addr(int id, uint64_t addr) {
  reg_write64(SIMPLENIC_RECV_REQ(id), addr);
}

static inline int nic_send_req_avail(int id)
{
  return reg_read32(SIMPLENIC_COUNTS(id)) & 0xff;
}

static inline int nic_recv_req_avail(int id)
{
  return (reg_read32(SIMPLENIC_COUNTS(id)) >> 8) & 0xff;
}

static inline int nic_send_comp_avail(int id)
{
  return (reg_read32(SIMPLENIC_COUNTS(id)) >> 16) & 0xff;
}

static inline int nic_recv_comp_avail(int id)
{
  return (reg_read32(SIMPLENIC_COUNTS(id)) >> 24) & 0xff;
}

static inline int nic_send_comp(int id)
{
  return reg_read16(SIMPLENIC_SEND_COMP(id));
}

static inline int nic_recv_comp(int id)
{
  return reg_read16(SIMPLENIC_RECV_COMP(id));
}

static inline float nic_ddio_rd_avg_lat(int nc) {
  uint64_t cycles = reg_read64(SIMPLENIC_BASE + 24 * nc + 8);
  uint64_t req_cnt = reg_read64(SIMPLENIC_BASE + 24 * nc + 16);
  if (req_cnt == 0)
    return -1;
  else
    return (float)(cycles / req_cnt);
}

static inline float nic_ddio_wr_avg_lat(int nc) {
  uint64_t cycles = reg_read64(SIMPLENIC_BASE + 24 * nc + 24);
  uint64_t req_cnt = reg_read64(SIMPLENIC_BASE + 24 * nc + 32);
  if (req_cnt == 0)
    return -1;
  else
   return (float)(cycles / req_cnt);
}

static void nic_send(void *data, unsigned long len, int id)
{
  uintptr_t addr = ((uintptr_t) data) & ((1L << 48) - 1);
  unsigned long packet = (len << 48) | addr;

  while (nic_send_req_avail(id) == 0);
  reg_write64(SIMPLENIC_SEND_REQ(id), packet);

  while (nic_send_comp_avail(id) == 0);
  reg_read16(SIMPLENIC_SEND_COMP(id));
}

static int nic_recv(void *dest, int id)
{
  uintptr_t addr = (uintptr_t) dest;
  int len;

  while (nic_recv_req_avail(id) == 0);
  reg_write64(SIMPLENIC_RECV_REQ(id), addr);

  // Poll for completion
  while (nic_recv_comp_avail(id) == 0);
  len = reg_read16(SIMPLENIC_RECV_COMP(id));
  asm volatile ("fence");

  return len;
}
