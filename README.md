AMAPVox
================

<img src="man/figures/logo.png" align="right" alt="" width="200" />

**LiDAR data voxelisation package**

## Description

AMAPvox tracks every laser pulse through 3D grid (voxelized space) and computes the local transmittance or local attenuation per voxel.
R package provides visualization, utility and validation tools for the voxelized space.

## Citation

To cite 'AMAPVox', please use citation(“AMAPVox”).

## Installation

Install from CRAN:

``` r
install.packages("AMAPVox")
```

Install from source:

- either with [pak](https://pak.r-lib.org/)

``` r
# from main repository
install.packages("pak")
pak::pkg_install("git::https://forge.ird.fr/amap/amapvox/AMAPVox.git")
```

- or with [remotes](https://remotes.r-lib.org/)

```
# from github mirror
install.packages("remotes")
remotes::install_github('umr-amap/AMAPVox')
```

To use it :

``` r
# launch AMAPVox GUI
AMAPVox::run()
```
