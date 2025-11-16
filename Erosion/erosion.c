#include <stdio.h>
#include <windows.h> // for high-precision timers

#define W 20
#define H 20

int main() {

  char input_chars[H][50] = {". . . . . . . . . . . . . . . . . . . .",
                             ". . . . . . . . . . . . . . . . . . . .",
                             ". . . . . . . . . . . . . . . . . . . .",
                             ". . . . . . . . . . . . . . . . . . . .",
                             ". . . . . . . . . . . @ . . . .",
                             ". . . . @ . . . . . . . . @ @ @ . . .",
                             ". . . @ @ @ . . . . . . @ @ @ @ @ . .",
                             ". . @ @ @ @ @ . . . . . . @ @ @ . . .",
                             ". . @ @ @ @ @ @ . . . . . . @ . . . .",
                             ". . @ @ @ @ @ @ . . . . . . . . . . .",
                             ". . @ @ @ @ @ @ . . . . . . . . . . .",
                             ". . . @ @ @ @ . . . . . . . . . . . .",
                             ". . . . @ @ . . . . . . . . . . . . .",
                             ". . . . @ . . . . . . @ @ @ . . . . .",
                             ". . . . . . . . . . . @ @ @ . . . . .",
                             ". . . . . . . . . . . @ @ @ . . . . .",
                             ". . . . . . . . . . . @ @ @ . . . . .",
                             ". . . . . . . . . . . . . . . . . . .",
                             ". . . . . . . . . . . . . . . . . . .",
                             ". . . . . . . . . . . . . . . . . . ."};

  unsigned char in_image[H][W];
  unsigned char out_image[H][W];

  // Convert ASCII input to image data
  for (int y = 0; y < H; y++) {
    int pos = 0;
    for (int x = 0; x < W; x++) {
      char c = input_chars[y][pos];
      pos += 2;
      in_image[y][x] = (c == '@') ? 255 : 0;
    }
  }

  // High-precision timer setup
  LARGE_INTEGER freq, t1, t2;
  QueryPerformanceFrequency(&freq);
  QueryPerformanceCounter(&t1);

  // ---- Perform erosion ----
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {

      if (x == 0 || y == 0 || x == W - 1 || y == H - 1) {
        out_image[y][x] = 0;
        continue;
      }

      if (in_image[y][x] == 0) {
        out_image[y][x] = 0;
      } else {
        if (in_image[y - 1][x] == 0 || in_image[y + 1][x] == 0 ||
            in_image[y][x - 1] == 0 || in_image[y][x + 1] == 0) {
          out_image[y][x] = 0;
        } else {
          out_image[y][x] = 255;
        }
      }
    }
  }

  QueryPerformanceCounter(&t2);

  // Time in microseconds
  double elapsed_us =
      (double)(t2.QuadPart - t1.QuadPart) * 1000000.0 / freq.QuadPart;

  printf("Execution time: %.3f microseconds\n\n", elapsed_us);

  // Print eroded output image
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      printf("%c ", out_image[y][x] ? '@' : '.');
    }
    printf("\n");
  }

  return 0;
}
