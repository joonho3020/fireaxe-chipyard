#include <stdio.h>
#include <stdlib.h>
#include <sched.h>
#include <pthread.h>
#include "encoding.h"
#include "marchid.h"


static int n_cores = 4;


void *thread_print_hello() {
/* int core = *((int*)id); */
  int core = 0;
  printf("Hello World from core %d\n", core);
  pthread_exit(NULL);
}

void __main() {
/* cpu_set_t cpuset[n_cores]; */
  pthread_t thread[n_cores];
  pthread_attr_t attr[n_cores];
/* pthread_barrier_t barrier; */
  printf("before init\n");

  for (int core = 0; core < n_cores; core++) {
    pthread_attr_init(&attr[core]);
    printf("attr_init: %d\n", core);
/* CPU_ZERO(&cpuset[core]); */
/* CPU_SET(core, &cpuset[core]); */
/* pthread_attr_setaffinity_np(&attr[core], sizeof(cpu_set_t), &cpuset[core]); */
/* pthread_create(&thread[core], &attr[core], thread_print_hello, (void*)&core); */
    if (pthread_create(&thread[core], NULL, thread_print_hello, NULL)) {
      printf("Failed to create thread for core %d\n", core);
      exit(1);
    }
    printf("create: %d\n", core);
  }
  for (int core = 0; core < n_cores; core++) {
    pthread_join(thread[core], NULL);
    printf("post join: %d\n", core);
  }
}


int main() {
  printf("main\n");
  __main();
  return 0;
}
