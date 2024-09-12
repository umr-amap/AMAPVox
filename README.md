[Github repository](https://github.com/umr-amap/AMAPVox) is mirrored from [<img src="https://forge.ird.fr/uploads/-/system/appearance/header_logo/1/Logo_IRD_Forge.png" height="15"> repository](https://forge.ird.fr/amap/amapvox/AMAPVox).

AMAPVox
================

<img src="man/figures/logo.png" align="right" alt="" width="200" />

**LiDAR data voxelisation package**

<https://amapvox.org>

## Description

AMAPvox tracks every laser pulse through 3D grid (voxelized space) and computes the local transmittance or local attenuation per voxel.

R package provides visualization, utility and validation tools for the voxelized space.

Handled point cloud formats:
- LAS/LAZ
- RXP/RSP (Riegl)
- PTX/PTG (Leica)
- XYB (Faro)

## Citation

To cite AMAPVox, please use `citation(“AMAPVox”)`.

Vincent, G., Antin, C., Laurans, M., Heurtebize, J., Durrieu, S., Lavalley, C., & Dauzat, J. (2017). Mapping plant area index of tropical evergreen forest by airborne laser scanning. A cross-validation study using LAI2200 optical sensor. _Remote Sensing of Environment_, 198, 254-266. <https://doi.org/10.1016/j.rse.2017.05.034>

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

## Contact

For questions, feedback and bug reporting `contact <at> amapvox.org`

Bug report page <https://forge.ird.fr/amap/amapvox/-/issues> or alternatively <https://github.com/umr-amap/AMAPVox/issues>

Senior scientist: Grégoire Vincent (IRD) `gregoire.vincent <at> ird.fr`

Main developper: Philippe Verley (IRD) `philippe.verley <at> ird.fr`

## Releases, changes history and source code

Releases and changes history: <https://forge.ird.fr/amap/amapvox/-/releases>

Source code: `git clone https://forge.ird.fr/amap/amapvox.git`

## Requirement

A 64 bit Operating System.

For `AMAPVox < 2.0.0` Java 1.8 is mandatory (Oracle or Corretto) and must be installed separately. For `AMAPVox >= 2.0.0` Java binary is included in the executable.

RiVLib library (RIEGL proprietary library for reading RSP/RXP scans) has only been compiled for Win64 and Linux_x86_64.

## License

AMAPVox is governed by the **CeCILL-B license** under French law and abiding by the rules of distribution of free software. You can  use, modify and/ or redistribute the software under the terms of the CeCILL-B license as circulated by CEA, CNRS and INRIA at the following URL <http://www.cecill.info>.

