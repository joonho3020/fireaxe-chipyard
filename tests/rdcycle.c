#include <stdio.h>
#include <inttypes.h>
#include <riscv-pk/encoding.h>


/* 
 * $ riscv64-unknown-elf-gcc -o rdcycle.riscv rdcycle.c 
*  $ spike pk rdcycle.riscv
*/

int main() {
  uint64_t start = rdcycle();
  printf("hello\n");
  uint64_t end = rdcycle();
  printf("%" PRIu64 " cycles\n", end - start);

  return 0;
}
