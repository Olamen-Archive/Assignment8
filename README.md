# Skip lists
Author: Zixuan Guo & Chris Won

## SkipList Time Complexity Analysis
To analyze the time complexity of the skiplist, we wrote a program that test the performance of skip list under given number of random datasets. In the program, we record the execution time for core operations (find, insert, and deletion). As we increasing the size of the data, we should be able to determine the time complexity from the pattern of increasing times consumption.

Experiment result:

| size     | set      | get   | remove |
| -------- |:--------:|:-----:| ------:|
| 100      | 2        | 0     | 0      |
| 1000     | 14       | 2     | 3      |
| 10000    | 24       | 24    | 30     |
| 100000   | 250      | 261   | 240    |
| 1000000  | 3425     | 2852  | 2779   |

We found that the increase of execution time appears to be logarithmic at first but then it increase linearly. we hypothesize that this was casued by increasing node height.
