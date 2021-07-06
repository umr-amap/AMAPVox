AMAPVox
================
**LiDAR data voxelisation package**

## Description

AMAPVox is an R package that provides a a set of functions for reading, manipulating and writing voxel spaces. Voxel spaces are read from text-based output files of the [AMAPVox software](http://www.amapvox.org).

As of version 0.1, available functions are limited and rudimentary, basically read/write voxel space. With time the package will include functions to launch full GUI tool from R and a set of useful pre/post-processing functions.

## Citation

To cite 'AMAPVox', please use citation(“AMAPVox”).

## Installation

AMAPVox package is not yet available on CRAN.
Install the latest version from Github:

``` r
install.packages("remotes")
remotes::install_github('umr-amap/AMAPVox')
```

To use it :

``` r
library("AMAPVox")
```
