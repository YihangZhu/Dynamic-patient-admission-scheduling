# Short term and long term objective compatibility in dynamic patient admission scheduling

This repository contains instances, solutions and validator related to the paper *Short term and long term objective compatibility in dynamic patient admission scheduling* by Yi-Hang Zhu, Túlio A.M. Toffolo, Wim Vancroonenburg and Greet Vanden Berghe
.

The dynamic patient admission scheduling problem was first introduced by *Ceschia, S., & Schaerf, A. (2016). Dynamic patient admission scheduling with operating room constraints, flexible horizons, and patient delays. Journal of Scheduling, 19(4), 377–389.*. Their expenrimental results are referred to https://bitbucket.org/satt/or-pas/.
 
##Instances

Generated instances are stored in folder `Instances` and `small_instances`. Each instance is validated again the `OrPasInstance.xsd` XML schema. 
They are split into 9 families with the following main features:

| Family | Departments | Rooms | OR Slots | Specialisms | Treatments | Patients | Days
| :----| ----:| ----:| ----:| ----:| ----:| ----:|  
| Small1 | 2 | 25    | 9  | 9   | 15  | 279-327   | 14    
| Small2 | 4 | 25    | 9  | 18  | 25  | 180-230   | 14    
| Small3 | 5 | 25-30 | 9  | 23  | 35  | 341-431   | 14    
| Short1 | 2 | 25 	 | 6  | 9   | 15  | 391-439   | 14
| Short2 | 4 | 50 	 | 11 | 18  | 25  | 574-644   | 14
| Short3 | 6 | 75 	 | 14 | 23  | 35  | 821-925   | 14
| Long1  | 2 | 25 	 | 6  | 9   | 15  | 693-762   | 28
| Long2  | 4 | 50 	 | 11 | 18  | 25  | 1089-1169 | 28
| Long3  | 6 | 75 	 | 14 | 23  | 35  | 1488-1602 | 28

## The lower bounds and the long term objective values from FS3 and FS2.

| Instance | FL2|    | FS2|  |FS3|     
| :------|  ----:|  ---:|  ---:|  ---:|  ---:|  ---:| 
|        | LB | Time | UB | Time | UB | Time      
| or_pas_dept2_short00 | 51047.55  | 1.77m   | 56768  | 232m   | 55484  |  268m     
| or_pas_dept2_short01 | 56011.58  | 1.37m   | 59873  | 7m     | 59177  |  16m     
| or_pas_dept2_short02 | 41646.96  | 0.68m   | 44910  | 8m     | 45341  |  125m     
| or_pas_dept2_short03 | 57436.78  | 2.24m   | 62377  | 3m     | 63147  |  9m     
| or_pas_dept2_short04 | 35935.52  | 1.74m   | 41857  | 7m     | 41791  |  11m     
| or_pas_dept4_short00 | 125077.61 | 4.61m   | 131873 | 15m    | 131173 |  34m     
| or_pas_dept4_short01 | 89073.93  | 7.13m   | 102437 | 10m    | 99678  |  88m     
| or_pas_dept4_short02 | 93415.34  | 10.72m  | 105231 | 39m    | 102535 |  91m     
| or_pas_dept4_short03 | 86254.06  | 3.96m   | 96939  | 4m     | 93814  |  7m     
| or_pas_dept4_short04 | 80941.67  | 5.81m   | 89440  | 9m     | 88175  |  9m     
| or_pas_dept6_short00 | 144169.58 | 14.65m  | 156117 | 199m   | 153411 |  180m     
| or_pas_dept6_short01 | 149322.43 | 25.63m  | 159846 | 112m   | 157150 |  241m     
| or_pas_dept6_short02 | 133404.12 | 32.59m  | 155153 | 133m   | 150620 |  241m     
| or_pas_dept6_short03 | 133269.24 | 25.09m  | 147500 | 24m    | 144750 |  77m     
| or_pas_dept6_short04 | 161868.86 | 23.49m  | 174280 | 94m    | 170659 |  234m     
| or_pas_dept2_long00  | 114825.75 | 33.86m  | 128792 | 98m    | 125624 |  206m     
| or_pas_dept2_long01  | 116219.85 | 37.36m  | 128352 | 807m   | 126691 |  930m     
| or_pas_dept2_long02  | 109967.35 | 16.78m  | 121919 | 66m    | 120833 |  107m     
| or_pas_dept2_long03  | 87809.44  | 27.86m  | 102322 | 246m   | 99744  |  339m     
| or_pas_dept2_long04  | 88026.19  | 41.30m  | 101150 | 697m   | 100452 |  964m     
| or_pas_dept4_long00  | 144027.61 | 56.33m  | 162363 | 33m    | 157291 |  187m     
| or_pas_dept4_long01  | 161444.85 | 106.43m | 186450 | 478m   | 181944 |  896m     
| or_pas_dept4_long02  | 187503.92 | 101.06m | 204618 | 133m   | 200185 |  674m     
| or_pas_dept4_long03  | 201786.35 | 226.31m | 226777 | 1030m  | 221124 |  1055m     
| or_pas_dept4_long04  | 150247.56 | 390.33m | 178919 | 1317m  | 172150 |  1372m     
| or_pas_dept6_long00  | 312311.74 | 1169.84m| 369848 | 1284m  | 359540 |  1309m     
| or_pas_dept6_long01  | 356361.57 | 2621.28m| 391993 | 1206m  | -      |  -     
| or_pas_dept6_long02  | 287883.17 | 643.93m | 321504 | 1222m  | 309882 |  1390m     
| or_pas_dept6_long03  | 325558.05 | 974.96m | 359537 | 1003m  | 350448 |  1110m     
| or_pas_dept6_long04  | 226336.82 | 218.02m | 251884 | 509m   | 244882 |  933m     



## Solutions

Solutions are available in the folders `solutions`.
Each solution is validated against the `OrPasSolution.xsd` XML schema.

## Validator

The solution validator is available as the C++ source file `or_pas_validator.cc`. The compilation command is provided on top of the file as a comment. The library `libxml++` needs to be installed.